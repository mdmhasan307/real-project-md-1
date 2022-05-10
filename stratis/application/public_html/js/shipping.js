

      function setFocusContainer() {
        var t = document.getElementById('txtStageLocator');
        if (t == null)
            t = document.getElementById('txtStageLocation');
        t.focus();
      }


// Used by Shipping_ManifestByAAC

  function confirmPrint (event) {
     var success = false;
     var output = '';
     var floor = document.getElementById('ManifestForm:AacNavList').value;
     if (floor == '')
        output += 'AAC-Floor Location - Must select a value\n';
        
    if (output != '') {
        alert (output);
    } else {
        success = true;
    }
     
     
     if (success) {
        // submit the manifest form
        window.submitForm('ManifestForm', 1, {source:'ManifestForm:submitManifestButton'});
     }
     return success;
  }
  
// Used by Shipping_ManifestShipment

              
  function printiFrame(){
    var objFrame=document.getElementById("manifestiFrame");
    var s = objFrame.src;
    if (objFrame.src != null)
    {
        if (s.indexOf(".jspx") != -1)
        {
            frames["manifestiFrame"].focus();
            frames["manifestiFrame"].print();
        }
    }
  }
         
  function confirmPrint (event) {
     var success = false;
     var output = '';
     var floor = document.getElementById('ManifestForm:FloorNavList').value;
     if (floor == '')
        output += 'Floor Location - Must select a value\n';
        
    if (output != '') {
        alert (output);
    } else {
        success = true;
    }
     
     
     if (success) {
        // submit the manifest form
        window.submitForm('ManifestForm', 1, {source:'ManifestForm:submitManifestButton'});
     }
     return success;
  }
  
// Shipping_ReviewContents


function setFocusReview() {

}
  
function submitYes1() {
    document.ReviewContentsForm.submitYesButton1_Javascript.click();
}
function submitNo1() {
    document.ReviewContentsForm.submitNoButton1_Javascript.click();
}
function submitYes2() {
    document.ReviewContentsForm.submitYesButton2_Javascript.click();
}            
function submitNo2() {
    document.ReviewContentsForm.submitNoButton2_Javascript.click();
}      
    