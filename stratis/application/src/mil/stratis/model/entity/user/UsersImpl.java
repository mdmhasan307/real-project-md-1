package mil.stratis.model.entity.user;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.RowIterator;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Number;
import oracle.jbo.domain.Timestamp;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class UsersImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    USER_ID {
      public Object get(UsersImpl obj) {
        return obj.getUserId();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setUserId((DBSequence) value);
      }
    },
    FIRST_NAME {
      public Object get(UsersImpl obj) {
        return obj.getFirstName();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setFirstName((String) value);
      }
    },
    MIDDLE_NAME {
      public Object get(UsersImpl obj) {
        return obj.getMiddleName();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setMiddleName((String) value);
      }
    },
    LAST_NAME {
      public Object get(UsersImpl obj) {
        return obj.getLastName();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setLastName((String) value);
      }
    },
    STATUS {
      public Object get(UsersImpl obj) {
        return obj.getStatus();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setStatus((String) value);
      }
    },
    LAST_LOGIN {
      public Object get(UsersImpl obj) {
        return obj.getLastLogin();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setLastLogin((Timestamp) value);
      }
    },
    USERNAME {
      public Object get(UsersImpl obj) {
        return obj.getUsername();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setUsername((String) value);
      }
    },
    VISIBLE_FLAG {
      public Object get(UsersImpl obj) {
        return obj.getVisibleFlag();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setVisibleFlag((String) value);
      }
    },
    LOGGED_IN {
      public Object get(UsersImpl obj) {
        return obj.getLoggedIn();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setLoggedIn((String) value);
      }
    },
    TEMP_KEY {
      public Object get(UsersImpl obj) {
        return obj.getTempKey();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setTempKey((String) value);
      }
    },
    LOGGED_IN_HH {
      public Object get(UsersImpl obj) {
        return obj.getLoggedInHh();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setLoggedInHh((String) value);
      }
    },
    TEMP_KEY_HH {
      public Object get(UsersImpl obj) {
        return obj.getTempKeyHh();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setTempKeyHh((String) value);
      }
    },
    LAST_LOGIN_HH {
      public Object get(UsersImpl obj) {
        return obj.getLastLoginHh();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setLastLoginHh((Timestamp) value);
      }
    },
    LOCKED {
      public Object get(UsersImpl obj) {
        return obj.getLocked();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setLocked((String) value);
      }
    },
    CAC_NUMBER {
      public Object get(UsersImpl obj) {
        return obj.getCacNumber();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setCacNumber((String) value);
      }
    },
    EFF_START_DT {
      public Object get(UsersImpl obj) {
        return obj.getEffStartDt();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setEffStartDt((Timestamp) value);
      }
    },
    EFF_END_DT {
      public Object get(UsersImpl obj) {
        return obj.getEffEndDt();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setEffEndDt((Timestamp) value);
      }
    },
    TRANS_TS {
      public Object get(UsersImpl obj) {
        return obj.getTransTs();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setTransTs((Timestamp) value);
      }
    },
    MOD_BY_ID {
      public Object get(UsersImpl obj) {
        return obj.getModById();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setModById((Number) value);
      }
    },
    USER_GROUPS {
      public Object get(UsersImpl obj) {
        return obj.getUserGroups();
      }

      public void put(UsersImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(UsersImpl object);

    public abstract void put(UsersImpl object, Object value);

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

  public static final int USERID = AttributesEnum.USER_ID.index();
  public static final int FIRSTNAME = AttributesEnum.FIRST_NAME.index();
  public static final int MIDDLENAME = AttributesEnum.MIDDLE_NAME.index();
  public static final int LASTNAME = AttributesEnum.LAST_NAME.index();
  public static final int STATUS = AttributesEnum.STATUS.index();
  public static final int LASTLOGIN = AttributesEnum.LAST_LOGIN.index();
  public static final int USERNAME = AttributesEnum.USERNAME.index();
  public static final int VISIBLEFLAG = AttributesEnum.VISIBLE_FLAG.index();
  public static final int LOGGEDIN = AttributesEnum.LOGGED_IN.index();
  public static final int TEMPKEY = AttributesEnum.TEMP_KEY.index();
  public static final int LOGGEDINHH = AttributesEnum.LOGGED_IN_HH.index();
  public static final int TEMPKEYHH = AttributesEnum.TEMP_KEY_HH.index();
  public static final int LASTLOGINHH = AttributesEnum.LAST_LOGIN_HH.index();
  public static final int LOCKED = AttributesEnum.LOCKED.index();
  public static final int CACNUMBER = AttributesEnum.CAC_NUMBER.index();
  public static final int EFFSTARTDT = AttributesEnum.EFF_START_DT.index();
  public static final int EFFENDDT = AttributesEnum.EFF_END_DT.index();
  public static final int TRANSTS = AttributesEnum.TRANS_TS.index();
  public static final int MODBYID = AttributesEnum.MOD_BY_ID.index();
  public static final int USERGROUPS = AttributesEnum.USER_GROUPS.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.user.Users");
  }

  /**
   * Gets the attribute value for UserId, using the alias name UserId.
   *
   * @return the value of UserId
   */
  public DBSequence getUserId() {
    return (DBSequence) getAttributeInternal(USERID);
  }

  /**
   * Sets <code>value</code> as the attribute value for UserId.
   *
   * @param value value to set the UserId
   */
  public void setUserId(DBSequence value) {
    setAttributeInternal(USERID, value);
  }

  /**
   * Gets the attribute value for FirstName, using the alias name FirstName.
   *
   * @return the value of FirstName
   */
  public String getFirstName() {
    return (String) getAttributeInternal(FIRSTNAME);
  }

  /**
   * Sets <code>value</code> as the attribute value for FirstName.
   *
   * @param value value to set the FirstName
   */
  public void setFirstName(String value) {
    setAttributeInternal(FIRSTNAME, value);
  }

  /**
   * Gets the attribute value for MiddleName, using the alias name MiddleName.
   *
   * @return the value of MiddleName
   */
  public String getMiddleName() {
    return (String) getAttributeInternal(MIDDLENAME);
  }

  /**
   * Sets <code>value</code> as the attribute value for MiddleName.
   *
   * @param value value to set the MiddleName
   */
  public void setMiddleName(String value) {
    setAttributeInternal(MIDDLENAME, value);
  }

  /**
   * Gets the attribute value for LastName, using the alias name LastName.
   *
   * @return the value of LastName
   */
  public String getLastName() {
    return (String) getAttributeInternal(LASTNAME);
  }

  /**
   * Sets <code>value</code> as the attribute value for LastName.
   *
   * @param value value to set the LastName
   */
  public void setLastName(String value) {
    setAttributeInternal(LASTNAME, value);
  }

  /**
   * Gets the attribute value for Status, using the alias name Status.
   *
   * @return the value of Status
   */
  public String getStatus() {
    return (String) getAttributeInternal(STATUS);
  }

  /**
   * Sets <code>value</code> as the attribute value for Status.
   *
   * @param value value to set the Status
   */
  public void setStatus(String value) {
    setAttributeInternal(STATUS, value);
  }

  /**
   * Gets the attribute value for LastLogin, using the alias name LastLogin.
   *
   * @return the value of LastLogin
   */
  public Timestamp getLastLogin() {
    return (Timestamp) getAttributeInternal(LASTLOGIN);
  }

  /**
   * Sets <code>value</code> as the attribute value for LastLogin.
   *
   * @param value value to set the LastLogin
   */
  public void setLastLogin(Timestamp value) {
    setAttributeInternal(LASTLOGIN, value);
  }

  /**
   * Gets the attribute value for Username, using the alias name Username.
   *
   * @return the value of Username
   */
  public String getUsername() {
    return (String) getAttributeInternal(USERNAME);
  }

  /**
   * Sets <code>value</code> as the attribute value for Username.
   *
   * @param value value to set the Username
   */
  public void setUsername(String value) {
    setAttributeInternal(USERNAME, value);
  }

  /**
   * Gets the attribute value for VisibleFlag, using the alias name VisibleFlag.
   *
   * @return the value of VisibleFlag
   */
  public String getVisibleFlag() {
    return (String) getAttributeInternal(VISIBLEFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for VisibleFlag.
   *
   * @param value value to set the VisibleFlag
   */
  public void setVisibleFlag(String value) {
    setAttributeInternal(VISIBLEFLAG, value);
  }

  /**
   * Gets the attribute value for LoggedIn, using the alias name LoggedIn.
   *
   * @return the value of LoggedIn
   */
  public String getLoggedIn() {
    return (String) getAttributeInternal(LOGGEDIN);
  }

  /**
   * Sets <code>value</code> as the attribute value for LoggedIn.
   *
   * @param value value to set the LoggedIn
   */
  public void setLoggedIn(String value) {
    setAttributeInternal(LOGGEDIN, value);
  }

  /**
   * Gets the attribute value for TempKey, using the alias name TempKey.
   *
   * @return the value of TempKey
   */
  public String getTempKey() {
    return (String) getAttributeInternal(TEMPKEY);
  }

  /**
   * Sets <code>value</code> as the attribute value for TempKey.
   *
   * @param value value to set the TempKey
   */
  public void setTempKey(String value) {
    setAttributeInternal(TEMPKEY, value);
  }

  /**
   * Gets the attribute value for LoggedInHh, using the alias name LoggedInHh.
   *
   * @return the value of LoggedInHh
   */
  public String getLoggedInHh() {
    return (String) getAttributeInternal(LOGGEDINHH);
  }

  /**
   * Sets <code>value</code> as the attribute value for LoggedInHh.
   *
   * @param value value to set the LoggedInHh
   */
  public void setLoggedInHh(String value) {
    setAttributeInternal(LOGGEDINHH, value);
  }

  /**
   * Gets the attribute value for TempKeyHh, using the alias name TempKeyHh.
   *
   * @return the value of TempKeyHh
   */
  public String getTempKeyHh() {
    return (String) getAttributeInternal(TEMPKEYHH);
  }

  /**
   * Sets <code>value</code> as the attribute value for TempKeyHh.
   *
   * @param value value to set the TempKeyHh
   */
  public void setTempKeyHh(String value) {
    setAttributeInternal(TEMPKEYHH, value);
  }

  /**
   * Gets the attribute value for LastLoginHh, using the alias name LastLoginHh.
   *
   * @return the value of LastLoginHh
   */
  public Timestamp getLastLoginHh() {
    return (Timestamp) getAttributeInternal(LASTLOGINHH);
  }

  /**
   * Sets <code>value</code> as the attribute value for LastLoginHh.
   *
   * @param value value to set the LastLoginHh
   */
  public void setLastLoginHh(Timestamp value) {
    setAttributeInternal(LASTLOGINHH, value);
  }

  /**
   * Gets the attribute value for Locked, using the alias name Locked.
   *
   * @return the value of Locked
   */
  public String getLocked() {
    return (String) getAttributeInternal(LOCKED);
  }

  /**
   * Sets <code>value</code> as the attribute value for Locked.
   *
   * @param value value to set the Locked
   */
  public void setLocked(String value) {
    setAttributeInternal(LOCKED, value);
  }

  /**
   * Gets the attribute value for CacNumber, using the alias name CacNumber.
   *
   * @return the value of CacNumber
   */
  public String getCacNumber() {
    return (String) getAttributeInternal(CACNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for CacNumber.
   *
   * @param value value to set the CacNumber
   */
  public void setCacNumber(String value) {
    setAttributeInternal(CACNUMBER, value);
  }

  /**
   * Gets the attribute value for EffStartDt, using the alias name EffStartDt.
   *
   * @return the value of EffStartDt
   */
  public Timestamp getEffStartDt() {
    return (Timestamp) getAttributeInternal(EFFSTARTDT);
  }

  /**
   * Sets <code>value</code> as the attribute value for EffStartDt.
   *
   * @param value value to set the EffStartDt
   */
  public void setEffStartDt(Timestamp value) {
    setAttributeInternal(EFFSTARTDT, value);
  }

  /**
   * Gets the attribute value for EffEndDt, using the alias name EffEndDt.
   *
   * @return the value of EffEndDt
   */
  public Timestamp getEffEndDt() {
    return (Timestamp) getAttributeInternal(EFFENDDT);
  }

  /**
   * Sets <code>value</code> as the attribute value for EffEndDt.
   *
   * @param value value to set the EffEndDt
   */
  public void setEffEndDt(Timestamp value) {
    setAttributeInternal(EFFENDDT, value);
  }

  /**
   * Gets the attribute value for TransTs, using the alias name TransTs.
   *
   * @return the value of TransTs
   */
  public Timestamp getTransTs() {
    return (Timestamp) getAttributeInternal(TRANSTS);
  }

  /**
   * Sets <code>value</code> as the attribute value for TransTs.
   *
   * @param value value to set the TransTs
   */
  public void setTransTs(Timestamp value) {
    setAttributeInternal(TRANSTS, value);
  }

  /**
   * Gets the attribute value for ModById, using the alias name ModById.
   *
   * @return the value of ModById
   */
  public oracle.jbo.domain.Number getModById() {
    return (oracle.jbo.domain.Number) getAttributeInternal(MODBYID);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModById.
   *
   * @param value value to set the ModById
   */
  public void setModById(oracle.jbo.domain.Number value) {
    setAttributeInternal(MODBYID, value);
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
   * @return the associated entity oracle.jbo.RowIterator.
   */
  public RowIterator getUserGroups() {
    return (RowIterator) getAttributeInternal(USERGROUPS);
  }

  /**
   * @param userId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence userId) {
    return new Key(new Object[]{userId});
  }
}
