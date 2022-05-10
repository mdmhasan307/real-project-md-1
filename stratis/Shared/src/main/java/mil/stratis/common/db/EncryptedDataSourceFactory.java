package mil.stratis.common.db;

import mil.stratis.common.util.crypto.CustomEncryptor;
import oracle.ucp.jdbc.PoolDataSourceImpl;

import javax.naming.*;
import java.util.Hashtable;

/**
 * Extends {@link PoolDataSourceImpl} to provide the ability to store database credentials encrypted.
 * <p>
 * Extracts the encrypted datasource password from the Tomcat context file and replaces it with the decrypted value.
 * Update the "factory" attribute from the Tomcat context file to point to this class. Also, update the "password"
 * attribute to the encrypted password... should be used to generate this password.
 * An example context file can be found in {tomcat8/conf/Catalina/localhost/stratweb70401.xml}
 */
public class EncryptedDataSourceFactory extends PoolDataSourceImpl {

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getObjectInstance(Object object, Name name, Context context, Hashtable environment) throws Exception {
    if (object instanceof Reference) {
      Reference reference = (Reference) object;
      replacePasswordIfEncrypted(reference);

      return super.getObjectInstance(object, name, context, environment);
    }
    else {
      throw new IllegalArgumentException(
          "Expecting javax.naming.Reference as object type not " + object.getClass().getName());
    }
  }

  // Search for password and replace with decrypted value if it's stored encrypted
  private void replacePasswordIfEncrypted(final Reference reference) {
    if (reference == null) throw new IllegalArgumentException("Reference object must not be null");

    for (int i = 0; i < reference.size(); i++) {
      String fieldKey = "password";
      RefAddr addr = reference.get(i);

      if (fieldKey.equals(addr.getType())) {
        if (addr.getContent() == null) throw new IllegalArgumentException("Password value must not be null for key " + fieldKey);

        String password = addr.getContent().toString();

        // Decrypt if password is stored encrypted
        // NOTE: Encrypted passwords should be surrounded with "ENC()"
        if (password.startsWith("ENC(")) {
          String encryptedPassword = password.substring(password.indexOf('(') + 1, password.indexOf(')'));
          String decrypted = CustomEncryptor.decrypt(encryptedPassword);
          reference.remove(i);
          reference.add(i, new StringRefAddr(fieldKey, decrypted));
        }

        break;
      }
    }
  }
}

