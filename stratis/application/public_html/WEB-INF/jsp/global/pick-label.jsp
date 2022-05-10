<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Picking Label</title>
    <link rel="stylesheet"  href="${urlContextPath}/css/label.css"></link>
</head>
<body>
<form id="closeForm" class="close-form" method="POST">
    <button type="button" class="close-button" id="btnExit" onclick="window.close();">Close After Printing</button>
</form>
<c:forEach var="pick" items="${pickLabels}">
    <div class="container">
        <table class="label" aria-describedby="label">
            <tr>
                <td>
                    STRATIS ${pick.mechStation} STOW: ${pick.city}<br/>
                    PACKING STATION: ${pick.packingStation}<br/>
                    FSC/NIIN:  ${pick.fscniin} CC: ${pick.cc}<br/>
                    DOC NO: ${pick.docno} SCN: ${pickscn}<br/>
                    PIN: ${pick.pin} PIN QTY: ${pick.pinQty} <br/>
                    USER NAME: ${pick.username} DATE/TIME: ${pick.datetime}<br/>
                    DESC: ${pick.nomenclature}
                </td>
            </tr>
            <tr class="bottom-row">
                <td>
                    <div class="barcode"><img src='${urlContextPath}/SlotImage?type=BARCODE&bc=${pick.pin}&bt=LABEL&random=1' alt="barcode"/></div>
                    <div class="barcode-text">${pick.pin}</div>
                </td>
            </tr>
        </table>
    </div>
</c:forEach>
<script>
    window.print();
</script>
</body>
</html>
