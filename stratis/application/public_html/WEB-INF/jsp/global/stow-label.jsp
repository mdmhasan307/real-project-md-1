<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
  <title>Stowing Label</title>
  <link rel="stylesheet"  href="${urlContextPath}/css/label.css"></link>
</head>
<body>
<form id="closeForm" class="close-form" method="POST">
  <button type="button" class="close-button" id="btnExit" onclick="window.close();">Close After Printing</button>
</form>
<c:forEach var="stow" items="${stowLabels}">
  <div class="container">
    <table class="label" aria-describedby="label">
      <tr>
        <td colspan="3">
          STRATIS ${stow.mechStation} STOW: ${stow.city}<br/>
          LOCATION: ${stow.location}<br/>
          FSC/NIIN: ${stow.fscniin} U/I: ${stow.ui} CC: ${stow.cc}<br/>
          DOC NO: ${stow.docno} RCN: ${stow.rcn}<br/>
          SID: ${stow.sid} SID QTY: ${stow.sidQty} EXP: ${stow.exp}<br/>
          LOT: ${stow.lot} CASE QTY: ${stow.caseQty} SERIAL NUM: ${stow.serialNum} <br/>
          USER NAME: ${stow.username} DATE/TIME: ${stow.datetime}<br/>
          DESC: ${stow.nomenclature}
        </td>
      </tr>
      <tr class="bottom-row">
        <td><span class="big-text">${stow.wacNumber}</span></td>
        <td>
          <div class="barcode"><img src='${urlContextPath}/SlotImage?type=BARCODE&bc=${stow.sid}&bt=LABEL&random=1' alt="barcode"/></div>
          <div class="barcode-text">${stow.sid}</div>
        </td>
        <td><span class="big-text">CC:${stow.cc}</span></td>
      </tr>
    </table>
  </div>
</c:forEach>
<script>
  window.print();
</script>

</body>
</html>
