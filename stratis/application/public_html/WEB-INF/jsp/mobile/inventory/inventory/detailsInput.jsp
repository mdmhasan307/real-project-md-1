<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:hidden path="inventoryItemId"/>
<form:hidden id="reconfirmQtyRequired" path="reconfirmQtyRequired"/>
<div class="flex-form-container">
    <div class="formItem canbesmall">
        <label for="location">Location</label><span class="required" title="Required Field">*</span>
        <form:input id="location" path="location" class="form-control form-control-sm" required="true"/>
    </div>
    <div class="formItem canbesmall">
        <label for="niin">Last two of NIIN</label><span class="required" title="Required Field">*</span>
        <form:input id="niin" maxlength="2" path="niin" class="form-control form-control-sm" required="true"/>
    </div>
    <div class="formItem canbesmall">
        <label for="locationQty">Location Quantity</label><span class="required" title="Required Field">*</span>
        <form:input type="number" min="0" id="locationQty" path="locationQty" class="form-control form-control-sm" required="true"/>
    </div>
    <div class="formItem canbesmall" id="reconfirmQtyInput">
        <label for="reconfirmLocationQty">Reconfirm Quantity</label><span class="required" title="Required Field">*</span>
        <form:input type="number" min="0" id="reconfirmLocationQty" path="reconfirmLocationQty" class="form-control form-control-sm"/>
    </div>
</div>
<c:if test="${serialControlled}">
    <div class="flex-form-container">
        <div class="flex-form-container">
            <div class="formItem">
                <label for="serial">Serial</label>
                <form:input type="text" path="serial" placeholder="serial" class="form-control form-control-sm"/>
                <div class="flex-button-container">
                    <div class="flex-button-group singleButton">
                        <button type="button" class="btn btn-primary" id="btnAdd">Add Serial</button>
                    </div>
                </div>
            </div>
            <div class="formItem">
                <label for="serials">Serial List</label>
                <div>
                    <ul class="list-group custom" id="serialListGroup">
                    </ul>
                </div>
                <select id="serials" name="serials" size="10" multiple="multiple" style="display:none"
                        class="input_and_button_height w-100"> </select>
            </div>
        </div>
    </div>
</c:if>
<div class="flex-form-container">
    <div class="formItem smallItem">
        <label for="locationLabel">Location</label>
        <input type="text" id="locationLabel" value='<c:out value="${inventoryItem.niinLocation.location.locationLabel}"/>'
               readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem smallItem">
        <label for="niinDisp">NIIN</label>
        <input type="text" id="niinDisp" value='<c:out value="${inventoryItem.niinLocation.niinInfo.niin}" />'
               readonly class="form-control-plaintext"/>
    </div>
    <div class="formItem">
        <label for="nomenclature">Nomenclature</label>
        <input type="text" id="nomenclature" value='<c:out value="${inventoryItem.niinLocation.niinInfo.nomenclature}" />'
               readonly class="form-control-plaintext"/>
    </div>
</div>


