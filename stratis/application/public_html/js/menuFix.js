//Code to fix the tab menu to the left when tabs are not rendered
document.addEventListener("DOMContentLoaded", function(){
    var elements = document.getElementsByClassName("navTab");
    var totalLength = 0
    for (var e = 0;  elements.length > e  ; e++) {
        totalLength += elements.item(e).clientWidth-15; //account for blank space in the rounded corner images
    }
    var navBar = document.getElementsByClassName("navBar")[0];
    if (navBar) {
        navBar.style.width = totalLength + "px";
    }
})