// find component method helper
function fod_findComponent(compName, rootComp){
    var result = null;
    
    // search the component subtree first.
    if(rootComp != null){
        result = rootComp.findComponent(compName);
    } 
    
    // if we did not find the comp in the 
    // subtree search the rest of the page.
    if(result == null || rootComp == null){
        result = AdfPage.findComponent(compName);
    }
    
    return result;
}

function fod_showPopup(event){
    var sourceComp = event.getSource();
    var popupId = sourceComp.getProperty("popupId");
    var hints = sourceComp.getProperty("hints");
    var propagateToServer = sourceComp.getProperty("propagateToServer");
    var popup = fod_findComponent(popupId, sourceComp);    
    
    if(propagateToServer != null && !propagateToServer){
        alert('cancel');
        event.cancel();
    }
    
    if(popup != null){
        if(hints != null){
            popup.show(hints);	
        } else {
            popup.show();
        }
    }
}

function fod_hidePopup(event){
    var sourceComp = event.getSource();
    var popupId = sourceComp.getProperty("popupId");
    var propagateToServer = sourceComp.getProperty("propagateToServer");
    var popup = fod_findComponent(popupId, sourceComp);    

    if(propagateToServer != null && !propagateToServer){
        event.cancel();
    }

    if(popup != null){
        popup.hide();
    }
}
