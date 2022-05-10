package exmlservice;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class StratisNamespaceMapper extends NamespacePrefixMapper {

    private static final String STRATIS_URI = "http://www.usmc.mil/schemas/1/if/stratis";

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        return "";
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { STRATIS_URI };
    }
}