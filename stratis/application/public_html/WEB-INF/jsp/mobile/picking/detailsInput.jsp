<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:hidden path="pid"/>
<div class="flex-form-container">
    <c:if test="${!param.bypassOrRefuse}">
        <form:hidden id="partialPickAttempted" path="partialPickAttempted"/>
        <div class="formItem canbesmall">
            <label for="location">Location</label><span class="required" title="Required Field">*</span>
            <form:input id="location" path="location" class="form-control form-control-sm" required="true"/>
        </div>
        <div class="formItem canbesmall">
            <label for="niin">Last 2 of NIIN</label><span class="required" title="Required Field">*</span>
            <form:input id="niin" maxlength="2" path="niin" class="form-control form-control-sm" required="true"/>
        </div>
        <div class="formItem canbesmall">
            <label for="pickQty">Pick Quantity</label><span class="required" title="Required Field">*</span>
            <form:input pattern="[0-9]{1,6}" id="pickQty" path="pickQty"
                        class="form-control form-control-sm" title="Quantity must be between 0 and 999999"
                        required="true"/>
        </div>
        <c:if test="${showInventory}">
            <c:choose>
                <c:when test="${param.editable}">
                    <div class="formItem canbesmall">
                        <label for="inventoryCount">Inventory Count</label><span class="required" title="Required Field">*</span>
                        <form:input pattern="[0-9]{1,6}" id="inventoryCount" path="inventoryCount"
                                    class="form-control form-control-sm" title="Count must be between 0 and 999999"
                                    required="true"/>
                    </div>
                    <div class="formItem canbesmall">
                        <label for="reInventoryCount">Re-Confirm Inventory</label><span class="required" title="Required Field">*</span>
                        <form:input pattern="[0-9]{1,6}" id="reInventoryCount" path="reInventoryCount"
                                    class="form-control form-control-sm" title="Re-Count must be between 0 and 999999"
                                    required="true"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Hidden fields to pass along the inventory values to PickingControlledNiinDetailController the values would be null otherwise -->
                    <form:hidden path="inventoryCount"/>
                    <form:hidden path="reInventoryCount"/>
                </c:otherwise>
            </c:choose>
        </c:if>
        <div class="formItem">
            <label for="pin">3rd Party Pin</label>
            <form:input id="pin"
                        path="pin"
                        maxlength="10"
                        pattern="[a-zA-Z0-9]{5,10}"
                        title="Pin must be between 5 and 10 character alphanumeric"
                        class="form-control form-control-sm"/>
        </div>
        <div class="formItem canbesmall">
            <label for="mcpx">MCPX Only</label>
            <c:choose>
                <c:when test="${param.editable}">
                    <form:select path="mcpx" id="mcpx" class="form-control form-control-sm">
                        <form:option value="No">No</form:option>
                        <form:option value="Yes">Yes</form:option>
                    </form:select>
                </c:when>
                <c:otherwise>
                    <form:input id="mcpx" path="mcpx" class="form-control-plaintext"/>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>
<div class="flex-form-container">
    <div class="formItem smallItem">
        <label for="scn">SCN</label>
        <input type="text" id="scn" value='<c:out value="${issue.scn}" />' readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem">
        <label for="locLabel">Loc Label</label>
        <input type="text" id="locLabel" value='<c:out value="${pick.niinLocation.location.locationLabel}" />' readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem">
        <label for="niinDisp">NIIN</label>
        <input type="text" id="niinDisp" value='<c:out value="${pick.niinLocation.niinInfo.niin}" />' readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem">
        <label for="documentId">Document ID</label>
        <input type="text" id="documentId" value='<c:out value="${issue.documentId}" />' readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem">
        <label for="pickQtyDisp">Pick Quantity</label>
        <input type="text" id="pickQtyDisp" value='<c:out value="${pick.pickQty}" />' readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem">
        <label for="qtyToBePicked">Qty to Pick</label>
        <input type="text" id="qtyToBePicked" value='<c:out value="${pick.pickQty - pick.qtyPicked}" />' readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem">
        <label for="ui">UI</label>
        <input type="text" id="ui" value='<c:out value="${pick.niinLocation.niinInfo.ui}" />' readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem">
        <label for="cc">CC</label>
        <input type="text" id="cc" value='<c:out value="${pick.niinLocation.cc}" />' readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem">
        <label for="nomenclature">Nomenclature</label>
        <input type="text" id="nomenclature" value='<c:out value="${pick.niinLocation.niinInfo.nomenclature}" />' readonly class="form-control-plaintext"/>
    </div>
</div>


<script>
    $(document).ready(function () {
        if (${param.editable}) {
            $(".required").show();
        } else {
            $(".required").hide();
            $(".canbesmall").addClass("smallItem");

            $("#location").prop("readonly", true);
            $("#location").removeClass();
            $("#location").addClass("form-control-plaintext");

            $("#niin").prop("readonly", true);
            $("#niin").removeClass();
            $("#niin").addClass("form-control-plaintext");

            $("#pickQty").prop("readonly", true);
            $("#pickQty").removeClass();
            $("#pickQty").addClass("form-control-plaintext");

            $("#inventoryCount").prop("readonly", true);
            $("#inventoryCount").removeClass();
            $("#inventoryCount").addClass("form-control-plaintext");

            $("#reInventoryCount").prop("readonly", true);
            $("#reInventoryCount").removeClass();
            $("#reInventoryCount").addClass("form-control-plaintext");

            $("#pin").prop("readonly", true);
            $("#pin").removeClass();
            $("#pin").addClass("form-control-plaintext");

            $("#mcpx").prop("readonly", true);
            $("#mcpx").removeClass();
            $("#mcpx").addClass("form-control-plaintext");
        }
    });
</script>
