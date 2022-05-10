<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Barcode</title>
    <link rel="stylesheet"  href="${urlContextPath}/css/label.css"></link>
</head>
<body>
<form id="closeForm" class="close-form" method="POST">
    <button type="button" class="close-button" id="btnExit" onclick="window.close();">Close After Printing</button>
</form>
<c:forEach var="label" items="${barcodes}">
    <div class="container">
        <div class="center small-text">${label.title}</div>
        <div class="center"><img src='${urlContextPath}/SlotImage?type=BARCODE&bc=${label.barcode}&bt=BAR39&random=1' alt="barcode"></div>
    </div>
</c:forEach>
<script>
    window.print();
</script>
</body>
</html>
