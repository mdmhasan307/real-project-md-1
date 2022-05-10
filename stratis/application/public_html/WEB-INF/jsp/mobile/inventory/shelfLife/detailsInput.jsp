<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<div class="flex-form-container">
    <div class="formItem canbesmall">
        <label for="location">Location</label>
        <input type="text" id="location" value='<c:out value="${location}" />' readonly class="form-control-plaintext form-control-sm"/>
    </div>
    <div class="formItem canbesmall">
        <label for="niin">NIIN</label>
        <input type="text" id="niin" value='<c:out value="${niin}" />' readonly class="form-control-plaintext form-control-sm"/>
    </div>
    <div class="formItem smallItem">
        <label for="fsc">FSC</label>
        <input type="text" id="fsc" value='<c:out value="${fsc}" />' readonly class="form-control-plaintext form-control-sm"/>
    </div>
    <div class="formItem canbesmall">
        <label for="currDate">Current Exp Date</label>
        <input type="text" id="currDate" value='<c:out value="${currentExpirationDate}" />' readonly class="form-control-plaintext form-control-sm"/>
    </div>
    <div class="formItem canbesmall">
        <label for="newDate">New Exp Date</label>
        <input type="text" id="newDate" value='<c:out value="${newExpirationDate}" />' readonly class="form-control-plaintext form-control-sm"/>
    </div>
</div>


