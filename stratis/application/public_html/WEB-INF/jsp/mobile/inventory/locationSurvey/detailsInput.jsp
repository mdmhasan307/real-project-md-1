<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="stratis" uri="taglib.mil.usmc.stratis" %>
<%@ page import="mil.usmc.mls2.stratis.core.utility.DateConstants" %>
<form:hidden path="niinId"/>
<form:hidden path="niinLocationId"/>
<form:hidden path="inventoryItemId"/>
<div class="flex-form-container">
  <div class="formItem smallitem">
    <label for="cc">Condition Code</label>
    <span class="required" title="Required Field">*</span>
    <c:choose>
      <c:when test="${param.editable}">
        <form:select path="cc" id="cc" class="form-control form-control-lg" required="true">
          <option value="" disabled selected>Select a Condition Code</option>
          <form:options items="${conditionCodes}"
                        itemLabel="value"
                        itemValue="key"/>
        </form:select>
      </c:when>
      <c:otherwise>
        <form:input id="cc" path="cc" class="form-control-plaintext"/>
      </c:otherwise>
    </c:choose>
  </div>
  <div class="formItem smallitem">
    <label for="expirationDate">Expiration Date</label>
    <span class="required" title="Required Field">*</span>
    <form:input id="expirationDate"
                path="expirationDate"
                type="date"
                placeholder="${DateConstants.SITE_DATE_INPUT_FORMATTER_PATTERN}"
                class="form-control form-control-sm"
                title="${DateConstants.SITE_DATE_INPUT_FORMATTER_PATTERN}"
                pattern="${DateConstants.SITE_DATE_INPUT_FORMATTER_REGEX_PATTERN}"
                maxlength="10"
                required="true"/>
  </div>
</div>
<div class="flex-form-container">
  <div class="formItem smallItem">
    <label for="niin">NIIN</label>
    <input type="text"
           id="niin"
           value='<c:out value="${niinInfo.niin}" />'
           readonly
           class="form-control-plaintext"/>
  </div>
  <div class="formItem largeItem">
    <label for="nomenclature">Nomenclature</label>
    <input type="text"
           id="nomenclature"
           value='<c:out value="${niinInfo.nomenclature}" />'
           readonly
           class="form-control-plaintext"/>
  </div>
  <div class="formItem smallItem">
    <label for="locationLabel">Location</label>
    <input type="text"
           id="locationLabel"
           value='<c:out value="${location.locationLabel}" />'
           readonly
           class="form-control-plaintext"/>
  </div>
  <div class="formItem smallItem">
    <label for="side">Side</label>
    <input type="text"
           id="side"
           value='<c:out value="${location.side}" />'
           readonly
           class="form-control-plaintext"/>
  </div>
  <div class="formItem smallItem">
    <label for="bay">Bay</label>
    <input type="text"
           id="bay"
           value='<c:out value="${location.bay}" />'
           readonly
           class="form-control-plaintext"/>
  </div>
  <div class="formItem smallItem">
    <label for="locLevel">Loc Level</label>
    <input type="text"
           id="locLevel"
           value='<c:out value="${location.locLevel}" />'
           readonly
           class="form-control-plaintext"/>
  </div>
</div>

<script>
  $(document).ready(function () {
    var editable =
    ${param.editable}
    if (editable) {
      $(".required").show()
    } else {
      $(".required").hide()

      $("#cc")
          .prop("readonly", true)
          .removeClass()
          .addClass("form-control-plaintext")

      $("#expirationDate")
          .prop("readonly", true)
          .removeClass()
          .addClass("form-control-plaintext")
    }
  })
</script>
