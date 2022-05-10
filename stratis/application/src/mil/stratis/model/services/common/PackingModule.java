package mil.stratis.model.services.common;

import oracle.jbo.ApplicationModule;

import java.util.ArrayList;
import java.util.HashSet;

public interface PackingModule extends ApplicationModule {

  boolean isConsolStation(int workstationId);

  void clearPINList();

  int getPINcountForSCN(ArrayList scanList, String scn);

  HashSet processPINList(ArrayList scanList, HashSet scnSet);

  void refreshPINLoadDetail(int wId);

  int addPIN(String pin, int iWorkstationId);

  String getWorkstationName(int wId);

  String createConsolidationBarcode();

  String getBinColAndLev(int consolId);

  String getLocationBarcode(int consolId);

  int getConsolIdByPin(String pin);

  int autoCloseCarton(int consolId, int userId);

  int closeCarton(int consolId, int userId);

  boolean packConsolidatedIssue(int consolId, int userId);

  int reAssignCarton(int consolId, int newConsolId, int userId);

  boolean manualCloseCarton(int consolId, int userId);

  void clearCartonList();

  void displayCartons(int iWorkstationId);

  void clearPackList(int iWorkstationId, int isCancel);

  ArrayList fillPackingList(ArrayList scanList);
}
