<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stratis" uri="taglib.mil.usmc.stratis" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:hidden path="inventoryItemId"/>
<div class="flex-form-container">
  <div class="formItem smallitem">
    <label for="location">Location</label>
    <form:input id="location"
                path="location"
                class="form-control form-control-sm"
                title="Location must be 9 characters alphanumeric"
                maxlength="9"
                pattern="\w{9}"
                required="true"/>
  </div>
  <div class="formItem">
    <label for="niin">NIIN</label>
    <form:input id="niin"
                path="niin"
                class="form-control form-control-sm"
                title="NIIN must be 9 characters numeric"
                maxlength="9"
                pattern="\d{9}"
                required="true"/>
    <div class="flex-button-container">
      <div class="flex-button-group singleButton">
        <button type="button" class="btn btn-primary" id="btnAdd">Add NIIN</button>
      </div>
    </div>
  </div>
  <div class="formItem">
    <label for="scannedNiins">NIINs Surveyed</label>
    <div>
      <ul class="list-group custom" id="niinListGroup">
      </ul>
    </div>
  </div>
  <div class="formItem smallItem">
    <label for="locationLabel">Location</label>
    <input type="text" id="locationLabel" value='<c:out value="${currentInventoryItem.location.locationLabel}" />' readonly
           class="form-control-plaintext"/>
  </div>
  <div class="formItem smallItem">
    <label for="side">Side</label>
    <input type="text" id="side" value='<c:out value="${currentInventoryItem.location.side}" />' readonly class="form-control-plaintext"/>
  </div>
  <div class="formItem smallItem">
    <label for="bay">Bay</label>
    <input type="text" id="bay" value='<c:out value="${currentInventoryItem.location.bay}" />' readonly class="form-control-plaintext"/>
  </div>
  <div class="formItem smallItem">
    <label for="locLevel">Loc Level</label>
    <input type="text" id="locLevel" value='<c:out value="${currentInventoryItem.location.locLevel}" />' readonly class="form-control-plaintext"/>
  </div>
</div>
<div class="flex-form-container">
  <div class="table-responsive table-wrapper">
    <table class="table table-sm table-bordered table-scrollable">
      <tr>
        <th>Nomenclature</th>
        <th>Quantity</th>
        <th>Expiration Date</th>
      </tr>
      <c:forEach items="${allNiinLocations}" var="niinLocation">
        <tr id="${niinLocation.niinInfo.niin}">
          <td style="width: 45%">${niinLocation.niinInfo.nomenclature}</td>
          <td style="width: 5%">${niinLocation.qty}</td>
          <td style="width: 25%;"><stratis:localDateFormat date="${niinLocation.expirationDate}"/></td>
        </tr>
      </c:forEach>
    </table>
  </div>
  <select id="scannedNiins" name="scannedNiins" size="10" multiple="multiple" style="display:none"
          class="input_and_button_height w-100"> </select>
</div>
