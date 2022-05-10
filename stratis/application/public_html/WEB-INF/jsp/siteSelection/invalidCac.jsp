<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/jsp/global/head.jsp"/>
<html>
<head>
    <title>Invalid CAC</title>
</head>
<body>
<div id="bodyWrapper">
    <div id="securityBannerTop" class="securitybanner header green">UNCLASSIFIED / FOUO</div>
    <div class="page-wrapper chiller-theme">
        <main class="page-content" style="margin:20px 0 0 0;">
            <img src="${pageContext.request.contextPath}/resources/images/banner/TopBar_Left_long.jpg"/>
            <div class="flex-form-container">
                <div style="padding:10px;width:475px;background-color:#DCDCDC;opacity:0.85;">
                    <h2>No PIV Authentication Certificate was found.</h2>
                    <br/>
                    Ensure that your CAC is inserted into your smart card reader and that you have a valid PIV Authentication Certificate.
                    These certificates are required for login as of January 31, 2020.
                    <br/><br/>
                    If your CAC is inserted, you can try the following:
                    <ul><br/>
                        <li>Close your internet browser.</li>
                        <li>Open Internet Explorer, and go to the Tools - Internet Options - Content tab.</li>
                        <li>Click the 'Clear SSL State' button.</li>
                        <li>Close the Internet Options popup.</li>
                        <li>Navigate back to the STRATIS website.</li>
                        <li>You should be prompted to Select a Certificate -
                            <br/><br/>
                            On this popup, you need to select the certificate where the Issuer is a "<b>DOD ID CA</b>" certificate.
                            <br/><br/>
                            You will not be able to login to STRATIS if you select a certificate issued by a "<b>DOD EMAIL CA</b>".
                            <br/><br/>
                        </li>
                        <li>
                            After you have selected your certificate (issued by a "DOD ID CA"), click OK, and you should be able to log in
                            to
                            STRATIS.
                        </li>
                        <li>If you need further assistance, please visit <a
                                href="https://mls2support.com/">https://www.mls2support.com/</a></li>
                    </ul>
                </div>
            </div>
        </main>
    </div>
    <div id="securityBannerBottom" class="securitybanner footer green">UNCLASSIFIED / FOUO</div>
</div>
</body>
</html>
