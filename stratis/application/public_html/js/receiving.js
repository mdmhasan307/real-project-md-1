function tabNext1(checkFld, checkAmt,moveFld) {
    if(!e) var e = window.event;
    if (!((e.keyCode == 8) || (e.keyCode == 46)))
    {
     if (checkFld)
     {
      var fLen = checkFld.value.length;
      if (fLen &gt;= checkAmt)
      {
        if (moveFld)
        {
          moveFld.focus();
        } else alert('1');
      }
     } else alert('2');
    }
}
       
     
function prs2D(checkFld) {
    if(!e) var e = window.event;
    if ((e.keyCode == 30) || (e.keyCode == 29) || (e.keyCode == 4))
    {
     checkFld.value = checkFld.value + '%';
    }
 }
