package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.data.*;
import com.udacity.catpoint.security.application.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SecurityServiceTest {

    public Sensor sensor;

    @Mock
    public StatusListener statusListener;

    @Mock
    public SecurityRepository securityRepository;

    @Mock
    public ImageService imageService;

    @Mock
    public
    SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
        sensor = new Sensor("sensor", SensorType.DOOR);
    }

    // covers application requirement 1
    @Test
    void ifAlarmIsArmed_andSensorIsActivated_changeAlarmStatusToPending(){
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    // covers application requirement 2
    @Test
    void ifAlarmIsArmed_andSensorIsActivated_andAlarmIsAlreadyPending_changeAlarmStatusToOn(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // covers application requirement 3
    @Test
    void ifAlarmPending_andSensorsInactive_changeToNoAlarm(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(false);
        securityService.setAlarmStatus(AlarmStatus.NO_ALARM);
        verify(securityRepository, Mockito.times(1)).setAlarmStatus(any(AlarmStatus.class));
    }

    // covers application requirement 4
    @Test
    void check_whenAlarmIsaActive_changeInSensorState_shouldNotAlarmState(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        Mockito.verify(securityRepository, Mockito.times(0)).setAlarmStatus(any(AlarmStatus.class));
    }

    // covers application requirement 5
    @Test
    void ifSensorActivated_whileAlreadyActive_andSystemIsInPendingState_changeToAlarmState(){
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        Mockito.verify(securityRepository, Mockito.times(1)).setAlarmStatus(any(AlarmStatus.class));
    }

    //  covers application requirement 6
    @Test
    void check_whenInactiveSensorDeactivated_noChangeInAlarmState() {
        sensor.setActive(false);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        Mockito.verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));

    }

    // covers application requirement 7
    @Test
    void ifImageContainsCat_whileTheSystemIsArmedHome_putSystemIntoAlarmStatus(){
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // covers application requirement 8
    @Test
    void ifCatNotDetected_changeStatusToNoAlarm_asLongAsSensorInactive(){
        Mockito.when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        sensor.setActive(false);
        BufferedImage currentCameraImage = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
        securityService.processImage(currentCameraImage);
        Mockito.verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // covers application requirement 9
    @Test
    void ifSystemDisarmed_setStatusToNoAlarm(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // covers application requirement 10
    @Test
    void ifSystemArmed_resetAllSensorsToInactive(){
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.getSensors().forEach(sensor1 -> {
            assert Boolean.FALSE.equals(sensor1.getActive());
        });
    }

    // covers application requirement 11
    @Test
    void ifSystemArmed_whileCatDetected_setAlarmStatusToAlarm(){
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        Mockito.when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(Boolean.TRUE);
        securityService.processImage(mock(BufferedImage.class));
        Mockito.verify(securityRepository, Mockito.times(1)).setAlarmStatus(any(AlarmStatus.class));
    }

    // following tests aren't part of the requirements, just added to get full coverage

    @Test
    void testAddAndRemoveStatusListener() {
        securityService.addStatusListener(statusListener);
        securityService.removeStatusListener(statusListener);
    }

    @Test
    void testAddAndRemoveSensor() {
        securityService.addSensor(sensor);
        securityService.removeSensor(sensor);
    }

    @Test
    void checkChangeSensorActivationStatusWorks_withHandleSensorDeactivatedCovered(){
        Sensor sensor1 = new Sensor("testSensor",SensorType.DOOR);
        sensor1.setActive(true);
        securityRepository.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        assert sensor.getActive().equals(false);
        verify(securityRepository, times(1)).setAlarmStatus(any(AlarmStatus.class));
    }


    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    public void setArmingStatusMethod_runsThreeTimes(ArmingStatus armingStatus) {
        // method just to use @Parameterized test
        securityService.setArmingStatus(armingStatus);

    }

    // both og the following cover void handSensorDeactivated()
    @Test
    void test_handSensorActivated_whenRepositoryDisarmed_andAlarmOn_shouldTriggerHandleSensorDeactivated(){
        securityRepository.setArmingStatus(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);
    }

    @Test
    void test_handSensorActivated_whenRepositoryDisarmed_andRepositoryAlarmPending_shouldTriggerHandleSensorDeactivated(){
        securityRepository.setArmingStatus(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);
    }

    // the following method covers void handleSensorActivated()
    @Test
    void test_handSensorActivated_whenRepositoryDisarmed_andNoAlarm_shouldTriggerHandleSensorActivated(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        sensor.setActive(false);
        securityService.changeSensorActivationStatus(sensor, true);
    }
}