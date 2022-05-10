<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form:form id="manifestForm" method="POST">
    <div class="flex-form-container">
        <div class="formItem">
            <label for="floorLocation">Floor Location:</label><span class="required" title="Required Field">*</span>
            <input id="floorLocation" name="floorLocation" class="form-control form-control-sm" required="true"
               maxlength="5" pattern="\w*" title="Floor location must be 5 alphanumeric characters or less" />
        </div>
    </div>
    <c:if test="${ldCons != null && ldCons.size() > 0}">
        <div class="flex-form-container">
            <div class="table-responsive table-wrapper">
                <table class="table table-sm table-bordered">
                    <tr>
                        <th>Print Manifest(s) at Workstation for ${floor}</th>
                    </tr>
                    <c:forEach items="${ldCons}" var="ldcon">
                        <tr>
                            <td><c:out value="${ldcon}"/></td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
        </div>
    </c:if>
    <div class="flex-button-container">
        <div class="flex-button-group frmBtns">
            <button type="button" class="btn btn-primary" id="btnSubmit">Submit</button>
            <button type="button" class="btn btn-primary" id="btnMainMenu">Main Menu</button>
            <button type="button" class="btn btn-primary" id="btnExit">Exit</button>
        </div>
    </div>
</form:form>
<script>
    var shippingManifest = new ShippingManifest(smvApp, "${requestScope['javax.servlet.forward.request_uri']}");
    shippingManifest.configureButtons();
</script>
