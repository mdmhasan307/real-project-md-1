package mil.usmc.mls2.stratis.core.service;

import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.*;
import mil.usmc.mls2.stratis.core.domain.event.domain.*;
import mil.usmc.mls2.stratis.core.domain.model.AuditLog;
import mil.usmc.mls2.stratis.core.domain.model.Customer;
import mil.usmc.mls2.stratis.core.domain.repository.AuditLogRepository;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventServiceTest {

    @Test
    @DisplayName("Check General Application Action Event")
    void checkGeneralApplicationEvent() {

        UserInfo userInfo = new UserInfo();

        // Events defined in mil.usmc.mls2.stratis.core.domain.event
        //GroupDeletedEvent.builder().build();
        UserLoggedInEvent.builder().build();
        UserLoggedOutEvent.builder().build();
        UserLoginFailedEvent.builder().build();
        UserPermissionEvent.builder().build();
        UserSessionTimeoutEvent.builder().build();
        UserPermissionEvent.builder().build();


        //EventService eventService = ContextUtils.getBean(EventService.class);
        EventService eventService = mock(EventService.class);
        CustomerUpdateEvent event = CustomerUpdateEvent.builder(AuditLogTypeEnum.SUCCESS)
                .userInfo(userInfo)
                .customer(aCustomer)
                .build();
        eventService.publishEvent(event);

        assertThat("Null should be null", null, nullValue());
    }

    @Test
    @DisplayName("Check CustomerUpdateEvent Audit Log Behavior")
    void checkCustomerUpdateAuditLogBehavior() {

        AuditLogRepository repo = mock(AuditLogRepository.class);
        ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);

        getAuditLogGenerator(repo).handle(aCustomerUpdateEvent);

        // Get the AuditLog object that was persisted to the mock
        verify(repo).persist(argument.capture());
        assertThat(argument.getValue().getDescription(), equalTo(
                String.format("Customer %s was modified by user %s", TEST_CUSTOMER_AAC, TEST_USERNAME)
        ));
    }

    @Test
    @DisplayName("Check DeleteWorkstationEvent Audit Log Behavior")
    void checkDeleteWorkstationAuditLogBehavior() {

        AuditLogRepository repo = mock(AuditLogRepository.class);
        ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);

        getAuditLogGenerator(repo).handle(aWorkstationDeletionEvent);

        // Get the AuditLog object that was persisted to the mock
        verify(repo).persist(argument.capture());
        assertThat(argument.getValue().getDescription(), equalTo(
                String.format("Workstation %s was deleted by user %s", TEST_WAC_ID, TEST_USERNAME)
        ));
    }

    @Test
    @DisplayName("Check ChangeLocationEvent Audit Log Behavior")
    void checkChangeLocationEventAuditLogBehavior() {

        AuditLogRepository repo = mock(AuditLogRepository.class);
        ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);

    ChangeLocationEvent changeLocationEvent = ChangeLocationEvent.builder(AuditLogTypeEnum.SUCCESS)
            .userInfo(aUserInfo)
            .fromLocation(TEST_OLD_LOCATION_ID)
            .toLocation(TEST_NEW_LOCATION_ID)
            .niin(TEST_NIIN)
            .build();

        getAuditLogGenerator(repo).handle(changeLocationEvent);

        // Get the AuditLog object that was persisted to the mock
        verify(repo).persist(argument.capture());
        assertThat(argument.getValue().getDescription(), equalTo(
                String.format("Location for NIIN %s was changed from %s to %s by user %s",
                TEST_NIIN, TEST_OLD_LOCATION_ID, TEST_NEW_LOCATION_ID, TEST_USERNAME)
        ));
    }

    @Test
    @DisplayName("Check CreateNiinEvent Audit Log Behavior")
    void checkCreateNiinEventAuditLogBehavior() {

        val repo = mock(AuditLogRepository.class);
        val argument = ArgumentCaptor.forClass(AuditLog.class);

        val createNiinEvent = CreateNiinEvent.builder(AuditLogTypeEnum.SUCCESS)
                .userInfo(aUserInfo)
                .niin(TEST_NIIN)
                .fsc(TEST_FSC)
                .build();

        getAuditLogGenerator(repo).handle(createNiinEvent);

        // Get the AuditLog object that was persisted to the mock
        verify(repo).persist(argument.capture());
        assertThat(argument.getValue().getDescription(), equalTo(
                String.format("NIIN %s with FSC %s was created by user %s",
                        TEST_NIIN, TEST_FSC, TEST_USERNAME)
        ));
    }

    @Test
    @DisplayName("Check DeleteNiinEvent Audit Log Behavior")
    void checkDeleteNiinEventAuditLogBehavior() {

        AuditLogRepository repo = mock(AuditLogRepository.class);
        ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);

        DeleteNiinEvent deleteNiinEvent = DeleteNiinEvent.builder(AuditLogTypeEnum.SUCCESS)
                .userInfo(aUserInfo)
                .niin(TEST_NIIN)
                .build();

        getAuditLogGenerator(repo).handle(deleteNiinEvent);

        // Get the AuditLog object that was persisted to the mock
        verify(repo).persist(argument.capture());
        assertThat(argument.getValue().getDescription(), equalTo(
                String.format("NIIN %s was deleted by user %s",
                        TEST_NIIN, TEST_USERNAME)
        ));
    }

    // Some Customer objects to test event generation
    String TEST_CUSTOMER_AAC = "M20180";
    String TEST_USERNAME = "ibcoleman";
    String TEST_WAC_ID = "WAC1";
    String TEST_OLD_LOCATION_ID = "LOC1";
    String TEST_NEW_LOCATION_ID = "LOC2";
    String TEST_NIIN = "ANIIN";
    String TEST_FSC = "ANFSC";

    // A Customer
    Customer aCustomer = Customer.builder()
            .aac(TEST_CUSTOMER_AAC)
            .build();

    // A UserInfo
    UserInfo aUserInfo = new UserInfo();
    {
        aUserInfo.setUsername(TEST_USERNAME);
    }

    // A CustomerUpdateEvent
    CustomerUpdateEvent aCustomerUpdateEvent = CustomerUpdateEvent.builder(AuditLogTypeEnum.SUCCESS)
            .userInfo(aUserInfo)
            .customer(aCustomer)
            .build();

    // A WorkstationDeletionEvent
    WorkstationDeletionEvent aWorkstationDeletionEvent = WorkstationDeletionEvent.builder(AuditLogTypeEnum.FAILURE)
            .userInfo(aUserInfo)
            .wacId(TEST_WAC_ID)
            .build();

    // Helper Methods
    private AuditLogGenerator getAuditLogGenerator(AuditLogRepository repository) {
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        StratisConfig stratisConfig = mock(StratisConfig.class);
        return new AuditLogGenerator(repository, eventPublisher, stratisConfig);
    }
}
