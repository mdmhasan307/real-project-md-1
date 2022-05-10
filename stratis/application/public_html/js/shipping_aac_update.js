
function switchInput(ok) {                            
   hide(ok);
   show(ok);
}

function hide(num) {
    document.getElementById('acknowledgeInput0').style.display = 'none';
    document.getElementById('acknowledgeInput1').style.display = 'none';
}

function show(num) {
    var show = "acknowledgeInput" + num;
    document.getElementById(show).style.display = 'block';
}

function confirmPrint (event) {
  var success = false;
  var chkContainer = document.getElementById('PrintManifestForm:chkContainer');
  var chkAAC = document.getElementById('PrintManifestForm:chkAAC');
  var chkLDCON = document.getElementById('PrintManifestForm:chkLDCON');
  var noneChecked = true;
  var outputMsg = '';
  var selection = '';
  var num = 0;

    // check if any checkboxes are checked
    if (chkContainer.checked) {
        noneChecked = false;
        selection = document.getElementById('PrintManifestForm:ContainerNavList').value;
    if (selection != '')
    {
        num = document.getElementById('PrintManifestForm:txtContainerCopies').value;
        // validate number of copies                            
        if (num &gt; 0) {
            success = true;
        } else {
            outputMsg += 'Enter the number of copies to print for selected Container\n';
            success = false;
        }
    } else {                                
        outputMsg += 'Make a selection for Container\n';
        success = false;
    }                            
    
    }

    if (chkAAC.checked) {
        noneChecked = false;
    
        selection = document.getElementById('PrintManifestForm:AACNavList').value;
    if (selection != '')
    {
        num = document.getElementById('PrintManifestForm:txtAACCopies').value;
        // validate number of copies                            
        if (num &gt; 0) {
            success = true;
        } else {
            outputMsg += 'Enter the number of copies to print for selected AAC\n';
            success = false;
        }
    } else {                                
        outputMsg += 'Make a selection for AAC\n';
        success = false;
    }
    } 

    if (chkLDCON.checked) {
        noneChecked = false;
        
        selection = document.getElementById('PrintManifestForm:LDCONNavList').value;
        if (selection != '')
        {
            num = document.getElementById('PrintManifestForm:txtLDCONCopies').value;
            // validate number of copies                            
            if (num &gt; 0) {
                success = true;
            } else {
                outputMsg += 'Enter the number of copies to print for selected LD CON\n';
                success = false;
            }
        } else {
            outputMsg += 'Make a selection for LD CON\n';
            success = false;
        }
    }                        

    if (noneChecked) {
        alert('You must select something to print');
        success = false;
    }
    
    // submit the print form
    if (success) {
        window.submitForm('PrintManifestForm', 1, {source:'PrintManifestForm:submitPrintButton'});
        return true;
    } else {
    if (outputMsg != '')
        alert (outputMsg);
    }

    return false;
}
          