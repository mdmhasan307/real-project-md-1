<jsp:useBean id="userbean" class="mil.stratis.view.user.UserInfo" scope="session"/>

<%
    if (userbean.getUseHTMLPrint()) {
      //use html print option
%>
<script type="text/javascript">
    window.open('/stratis/app/mobile/print/print-label', 'PrintWindow');
</script>

<%
    }
%>
<%
    if (userbean.getUseprintcom().equals(1)) {
        //use applet print option
%>
<script type="text/javascript">
    window.open('/stratis/app/mobile/print', 'PrintWindow');
</script>

<%
    }
%>
