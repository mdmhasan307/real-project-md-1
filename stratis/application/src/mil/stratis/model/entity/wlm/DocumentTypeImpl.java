package mil.stratis.model.entity.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class DocumentTypeImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    DOCUMENT_ID {
      public Object get(DocumentTypeImpl obj) {
        return obj.getDocumentId();
      }

      public void put(DocumentTypeImpl obj, Object value) {
        obj.setDocumentId((String) value);
      }
    },
    DESCRIPTION {
      public Object get(DocumentTypeImpl obj) {
        return obj.getDescription();
      }

      public void put(DocumentTypeImpl obj, Object value) {
        obj.setDescription((String) value);
      }
    };
    private static final int FIRST_INDEX = 0;

    public abstract Object get(DocumentTypeImpl object);

    public abstract void put(DocumentTypeImpl object, Object value);

    public int index() {
      return AttributesEnum.firstIndex() + ordinal();
    }

    public static int firstIndex() {
      return FIRST_INDEX;
    }

    public static int count() {
      return AttributesEnum.firstIndex() + AttributesEnum.staticValues().length;
    }

    public static AttributesEnum[] staticValues() {
      return AttributesEnum.values();
    }
  }

  public static final int DOCUMENTID = AttributesEnum.DOCUMENT_ID.index();
  public static final int DESCRIPTION = AttributesEnum.DESCRIPTION.index();

  /**
   * Gets the attribute value for DocumentId, using the alias name DocumentId.
   *
   * @return the value of DocumentId
   */
  public String getDocumentId() {
    return (String) getAttributeInternal(DOCUMENTID);
  }

  /**
   * Sets <code>value</code> as the attribute value for DocumentId.
   *
   * @param value value to set the DocumentId
   */
  public void setDocumentId(String value) {
    setAttributeInternal(DOCUMENTID, value);
  }

  /**
   * Gets the attribute value for Description, using the alias name Description.
   *
   * @return the value of Description
   */
  public String getDescription() {
    return (String) getAttributeInternal(DESCRIPTION);
  }

  /**
   * Sets <code>value</code> as the attribute value for Description.
   *
   * @param value value to set the Description
   */
  public void setDescription(String value) {
    setAttributeInternal(DESCRIPTION, value);
  }

  /**
   * getAttrInvokeAccessor: generated method. Do not modify.
   *
   * @param index   the index identifying the attribute
   * @param attrDef the attribute
   * @return the attribute value
   */
  @Override
  protected Object getAttrInvokeAccessor(int index, AttributeDefImpl attrDef) throws Exception {
    if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
      return AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].get(this);
    }
    return super.getAttrInvokeAccessor(index, attrDef);
  }

  /**
   * setAttrInvokeAccessor: generated method. Do not modify.
   *
   * @param index   the index identifying the attribute
   * @param value   the value to assign to the attribute
   * @param attrDef the attribute
   */
  @Override
  protected void setAttrInvokeAccessor(int index, Object value, AttributeDefImpl attrDef) throws Exception {
    if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
      AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].put(this, value);
      return;
    }
    super.setAttrInvokeAccessor(index, value, attrDef);
  }

  /**
   * @param documentId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(String documentId) {
    return new Key(new Object[]{documentId});
  }

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.wlm.DocumentType");
  }
}
