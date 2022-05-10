package mil.stratis.view.admin;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.services.GCSSMCTransactionsImpl;
import mil.stratis.model.services.ImportFilesImpl;
import mil.stratis.view.util.ADFUtils;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.GcssImportsTaskExecutor;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.processor.gcss.*;
import mil.usmc.mls2.stratis.core.processor.gcss.event.FeedProcessedEvent;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;
import weblogic.utils.StringUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Backing bean for Admin_ProcessGCSSMCImports
 */
@Slf4j
public class GCSSMCImports extends AdminBackingHandler {

  private transient RichTable importTable;
  private transient RichOutputText importMessage;
  private transient RichOutputText viewCreated;
  private transient ImportState importState;

  public GCSSMCImports() {
    // Initialize our import state to allow the page to render properly
    importState = new ImportState(Type.NONE, Status.READY, "");
  }

  /**
   * Handler for the import button click.
   * Responsible for finding the selected file type
   * and kicking of an import with that type.
   *
   * @param action event sent from the UI (not used)
   */
  //FIXME due to the processes now running in threads the siteInterfacesIter doesn't get updated with any data during the process.
  //So if the user doesn't leave the screen the count doesn't decrease once the file is processed so this will not work as expected.
  public void importAction(ActionEvent action) {
    DCIteratorBinding siteInterfacesIter = ADFUtils.findIterator("GCSSMCSiteInterfacesView1Iterator");
    Row currentRow = siteInterfacesIter.getCurrentRow();
    String interfaceName = currentRow.getAttribute("InterfaceName").toString();
    Type type = Type.valueOf(interfaceName);
    val currentCount = Integer.parseInt(currentRow.getAttribute("TotalReady").toString());

    if (currentCount <= 0) {
      updateUI(new ImportState(type, Status.NO_RECORDS, null));
      return;
    }

    processImport(type);
  }

  /**
   * Calls the correct service based on the file type
   *
   * @param type file type to import
   */
  private void processImport(Type type) {
    log.debug("Beginning {} import.", type.name());

    ImportFilesImpl service = getImportFilesService();
    final String interfaceStatus = service.getSiteInterfaceStatus(type.name());

    if (interfaceStatus.equalsIgnoreCase("RUNNING")) {
      //the selected interface is already running.
      updateUI(new ImportState(type, Status.RUNNING, interfaceStatus));
      return;
    }

    val gcssImportsTaskExecutor = ContextUtils.getBean(GcssImportsTaskExecutor.class);
    val activeCount = gcssImportsTaskExecutor.getActiveCount();

    if (activeCount > 0) {
      //There is another feed already running.  Do not allow a second feed of any type.
      updateUI(new ImportState(type, Status.ANOTHER_FEED, null));
      return;
    }

    //FUTURE  UserSiteSelectionTracker updates to be re-usable elsewhere for threads with thread lifecycle aspect or similar
    val siteName = UserSiteSelectionTracker.getSiteName();
    val userSiteSelection = UserSiteSelectionTracker.getUserSiteSelection();

    EventService eventPublisher = ContextUtils.getBean(EventService.class);

    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

    val processFeed = (Runnable) () -> {

      UserSiteSelectionTracker.configureTracker(siteName, userSiteSelection);
      try {
        FeedProcessedEvent feedProcessedEvent = null;
        switch (type) {
          case DASF:
            val dasfProcessor = ContextUtils.getBean(DasfProcessor.class);
            val dasfResult = dasfProcessor.process();
            feedProcessedEvent = FeedProcessedEvent.builder()
                .messageTimestamp(OffsetDateTime.now())
                .feedType("DASF")
                .feedResults(dasfResult.returnStatistics())
                .session(session)
                .build();
            break;
          case GABF:
            val gabfProcessor = ContextUtils.getBean(GabfProcessor.class);
            val gabfResult = gabfProcessor.process();
            feedProcessedEvent = FeedProcessedEvent.builder()
                .messageTimestamp(OffsetDateTime.now())
                .feedType("GABF")
                .feedResults(gabfResult.returnStatistics())
                .session(session)
                .build();
            break;
          case GBOF:
            val gbofProcessor = ContextUtils.getBean(GbofProcessor.class);
            val gbofResult = gbofProcessor.process();
            feedProcessedEvent = FeedProcessedEvent.builder()
                .messageTimestamp(OffsetDateTime.now())
                .feedType("GBOF")
                .feedResults(gbofResult.returnStatistics())
                .session(session)
                .build();
            break;
          case MHIF:
            val mhifProcessor = ContextUtils.getBean(MhifProcessor.class);
            val mhifResult = mhifProcessor.process();
            feedProcessedEvent = FeedProcessedEvent.builder()
                .messageTimestamp(OffsetDateTime.now())
                .feedType("MHIF")
                .feedResults(mhifResult ? "Completed" : "Failed")
                .session(session)
                .build();
            break;
          case MATS:
            val matsProcessor = ContextUtils.getBean(MatsProcessor.class);
            val matsResult = matsProcessor.process();
            feedProcessedEvent = FeedProcessedEvent.builder()
                .messageTimestamp(OffsetDateTime.now())
                .feedType("MATS")
                .feedResults(matsResult.returnStatistics())
                .session(session)
                .build();
            break;
          case NONE:
          default:
            throw new IllegalStateException("Unsupported import type -- " + type.name());
        }
        eventPublisher.publishEvent(feedProcessedEvent);
      }
      finally {
        UserSiteSelectionTracker.clear();
      }
    };

    gcssImportsTaskExecutor.execute(processFeed);

    //wait 1 second.  This is to allow the thread to update the database site_interfaces table, so the updateUI updates the table data on screen for the user.
    try {
      TimeUnit.SECONDS.sleep(1);
    }
    catch (Exception e) {
      //no-op
    }
    //this needs to be run after the thread execute so the interface updates the table of data correctly.
    updateUI(new ImportState(type, Status.STARTED, interfaceStatus));
  }

  //only the Done, and Failure will pass extra data to the other UpdateUI.
  private void updateUI(ImportState newState) {
    updateUI(newState, null);
  }

  /**
   * Update the UI based upon the state of the import.
   * Slightly worthless now since the import service is run
   * on the main thread which doesn't allow UI updates to occur.
   *
   * @param newState changed state of the import
   */
  private void updateUI(ImportState newState, String additionalText) {

    final String interfaceStatusText = StringUtils.isEmptyString(newState.interfaceStatusText) ?
        "" : " Interface status: " + newState.interfaceStatusText;

    switch (newState.status) {
      case ANOTHER_FEED:
        failureMessage("There is another feed already running.  Please wait for the previous feed to finish before starting another one.");
        break;
      case READY:
        clearMessage();
        break;
      case NO_RECORDS:
        failureMessage(String.format("The %s feed does not have any records to process at this time.", newState.type.name()));
        break;
      case RUNNING:
        successMessage(String.format("The %s feed is currently running.  Check back later.", newState.type.name()));
        break;
      case STARTED:
        successMessage(String.format("The %s feed has been started.  You will be notified when it is complete.", newState.type.name()));
        break;
      case DONE:
        successMessage(newState.type.name() + " import successful." + interfaceStatusText + " " + additionalText);
        break;
      case FAILURE:
        failureMessage(newState.type.name() + " import failed." + interfaceStatusText + " " + additionalText);
        break;
      default:
        throw new IllegalStateException("Unsupported import status -- " + newState.status.name());
    }

    refreshImportTable();

    importState = newState;
  }

  /**
   * Helper method to refresh the file import table
   */
  private void refreshImportTable() {
    OperationBinding binding =
        ADFUtils
            .getDCBindingContainer()
            .getOperationBinding("ExecuteSiteInterface");

    binding.execute();
    AdfFacesContext.getCurrentInstance().addPartialTarget(importTable);
  }

  /**
   * Helper method clear the status message
   */
  private void clearMessage() {
    importMessage.setValue("");
    AdfFacesContext.getCurrentInstance().addPartialTarget(importMessage);
  }

  /**
   * Helper method to style and display a successful message
   */
  private void successMessage(String message) {
    importMessage.setValue(message);
    importMessage.setInlineStyle("color:green;font-weight:bolder;text-decoration:blink");
    AdfFacesContext.getCurrentInstance().addPartialTarget(importMessage);
  }

  /**
   * Helper method to style and display a failure message
   */
  private void failureMessage(String message) {
    importMessage.setValue(message);
    importMessage.setInlineStyle("color:#CC0000;font-weight:bolder;text-decoration:blink");
    AdfFacesContext.getCurrentInstance().addPartialTarget(importMessage);
  }

  // Simple enums and data class to manage the state of the import.
  // Also helps to cut down on the amount of code duplication.
  // Embedded directly in this class since this is the only place that they are used.
  // It would probably make sense in the future to use these in the export class as well
  // to cut down on duplication there
  enum Type {
    NONE,
    DASF,
    GABF,
    GBOF,
    MATS,
    MHIF
  }

  enum Status {
    READY,
    DONE,
    STARTED,
    RUNNING,
    NO_RECORDS,
    ANOTHER_FEED,
    FAILURE
  }

  class ImportState {

    Type type;
    Status status;
    String interfaceStatusText;

    ImportState(final Type type, final Status status, final String interfaceStatusText) {

      this.type = type;
      this.status = status;
      this.interfaceStatusText = interfaceStatusText;
    }
  }

  @Override
  public GCSSMCTransactionsImpl getGCSSMCTransactionsService() {
    return getGCSSMCAMService();
  }

  // Getters
  public RichTable getImportTable() { return importTable; }

  public RichOutputText getImportMessage() { return importMessage; }

  public RichOutputText getViewCreated() {
    // Calling updateUi on this hidden field because I don't know the proper hook for after view created
    updateUI(new ImportState(Type.NONE, Status.READY, ""));

    return viewCreated;
  }

  // Setters
  public void setImportTable(final RichTable importTable) { this.importTable = importTable; }

  public void setImportMessage(final RichOutputText importMessage) { this.importMessage = importMessage; }

  public void setViewCreated(final RichOutputText viewCreated) { this.viewCreated = viewCreated; }
}


