update site_info
set site_timezone = (
    select case trim(supply_center_name)
               when '1st MLG' then 'America/Los_Angeles'            /* CPEN */
               when 'SMU 2D MLG' then 'America/New_York'            /* CLNC */
               when 'ESD' then 'America/Los_Angeles'                /* ESD 29 Palms, CA */
               when '3D SUP BN BL 400 GA' then 'Japan'              /* OKI */
               when 'BLOUNT ISLAND COMMAND' then 'America/New_York' /* BIC */
               when 'CLB3 SUPCO BLDG 250' then 'US/Hawaii'          /* MCBH Hawaii */
               end
    from site_info);