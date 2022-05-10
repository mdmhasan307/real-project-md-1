
             
  function validateDriver() {
     var success = false;
     var mode = document.getElementById('subAcknowledgeForm:modes').value;
     var output = '';                

    if (mode == '')
        output += 'Acknowledge Pick Up - Must select a value\n';
        
    if (output != '') {
        alert (output);
        return false;
    }                       

             var driver = document.getElementById('subAcknowledgeForm:driver').value;
             if (driver == '')
                alert('You must enter a driver');
             else
                success = true;
     
     if (success) {
        success = confirm('Acknowledging Delivery will permanently kick issue to history.  Are you sure you want to continue?'); 
     }
     
     if (success)
        window.submitForm('subAcknowledgeForm', 1, {source:'subAcknowledgeForm:submitButton'});
  }
  
  function allCaps() {
    var txtAAC = document.getElementById('subLDCONForm:txtAAC');
    var txtLDCON = document.getElementById('subLDCONForm:txtLDCON');
    txtAAC.value = txtAAC.value.toString().toUpperCase();
    txtLDCON.value = txtLDCON.value.toString().toUpperCase();
  }
