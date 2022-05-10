      

function showMessage( blah ) {
    
    var output = '';
    
    output += blah;
    alert( output );
    
}

function showDOD_Banner(){
    var buf = 'You are accessing a U.S. Government (USG) Information Systems (IS) that is provided for USG-authorized use only.\n\n ';
    buf += 'By using this IS (which includes any device attached to this IS), you consent to the following conditions: \n\n';
    buf += ' -The USG routinely intercepts and monitors communications on this IS for purposes including but not limited to,\n';
    buf += '  penetration testing, COMSEC monitoring, network operations and defense, personnel misconduct (PM), \n';
    buf += '  law enforcement (LE), and counterintelligence (CI) investigations. \n\n';
    buf += ' -At any time, the USG may inspect and seize data stored on this IS. \n\n';
    buf += ' -Communications using, or data stored on, this IS are not private, are subject to routine monitoring,\n';
    buf += '  interception, and search, and may be disclosed or used for any USG authorized purpose. \n\n'; 
    buf += ' -This IS includes security measures (e.g., authentication and access controls) to protect USG interests\n';
    buf += '   --not for your personal benefit or privacy.\n\n';
    buf += ' -Notwithstanding the above, using this IS does not constitute consent to PM, LE or CI investigative searching\n';
    buf += '  or monitoring of the content of privileged communications, or work product, related to personal representation\n';
    buf += '  or services by attorneys, psychotherapists, or clergy, and their assistants. Such communications and work product\n';
    buf += '  are private and confidential. See User Agreement for details.';

    // if (request.getRequestedSessionId() == null ) { 
    
      // alert( 'show DOD Banner' );
      var b = confirm(buf); 
      if (!b) window.location.href = 'http://www.marines.com'; 
    // }
}

function validateWorkstation(cac) {
    // var success = false;
    var workstation = document.getElementById('selectWorkstation').value;
    var usertype = document.getElementById('selectUserType').value;
    var user = document.getElementById( 'selectUser' ).value;
    var output = '';                
    
    if (usertype == '')
        output += 'Login - Must select a role\n';
       
    if( user == 0 )
        output += 'Login = Must select a user\n';
    
    if (workstation == '')
        output += 'Login - Must select a workstation\n';
    
    if (output != '') {
        alert (output);
        return false;
    }                       
    
    if (cac == 0) {
        document.getElementById('submitCACButton').click();
    } else {
        document.getElementById('submitButton').click();
    }

}

function setFocusId(id) { 
    var t=document.getElementById(id); 
    if (t) t.focus(); 
}

