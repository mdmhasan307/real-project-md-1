### Overview
You can use these XSL files to transform the Fortify .fvdl data into a more usable
format. The .fpr file delivered by the PO has a lot of useful information, but it balls 
the vulnerabilities into a huge XML file. Since it's XML you can transform it into 
a more useful format with a few relatively simple steps.

### How To

Rename the .fpr file to .fpr.zip and extract it.

Rename the .fvdl file to .fvdl.xml and open it with a reasonable text editor.

Find the stanza <Vulnerabilities> and delete everything above it except the <xml> 
header.

Find the stanza </Vulnerabilities> and delete everything below it except the closing 
</xml> tag.

Once the fvdl file has been pruned to just include the <vulnerabilities> stanza, IDEA can use the .xsl file 
to transform the modified .fvdl file into a .csv file including a unique InstanceID that persists across scans, finding 
type, subtype, and class, path, and line info.

### Next Steps
Next step: Figure out why the .XSL file doesn't work on the "original" .fvdl.xml file even when I specify the exterior <FVDL> tag above the <Vulnerabilities> tag...
