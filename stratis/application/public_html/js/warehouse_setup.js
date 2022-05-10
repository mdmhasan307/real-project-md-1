
function removeEmptyOption() {
   if (document.buildingForm.siteSelectOne.options.length > 0) {
   if (document.buildingForm.siteSelectOne.options[0].value=='') {
      document.buildingForm.siteSelectOne.options[0] = null;
      document.buildingForm.siteSelectOne.value = document.buildingForm.siteSelectOne.options[0].value;
   }
   }
}
            
     