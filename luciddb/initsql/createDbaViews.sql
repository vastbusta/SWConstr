create schema localdb.sys_root;
set schema 'localdb.sys_root';
set path 'localdb.sys_root';

create view dba_schemas as
  select
    catalog_name,
    schema_name,
    ai."name" as creator,
    creation_timestamp,
    last_modified_timestamp,
    remarks,
    si."mofId" as mofid,
    si."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_schemas_internal2 si
  inner join
    sys_fem."Security"."AuthId" ai
  on
    si."Grantee" = ai."mofId"
;

grant select on dba_schemas to public;

create view dba_tables as
  select
    catalog_name,
    schema_name,
    table_name,
    table_type,
    ai."name" as creator,
    creation_timestamp,
    last_modification_timestamp,
    remarks,
    dti."mofId" as mofid,
    dti."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_tables_internal2 dti
  inner join
    sys_fem."Security"."AuthId" ai
  on
    dti."Grantee" = ai."mofId"
;

grant select on dba_tables to public;

create view dba_columns as
  select
    table_cat as catalog_name,
    table_schem as schema_name,
    table_name,
    column_name,
    ordinal_position,
    dt."name" as datatype,
    column_size as "PRECISION",
    dec_digits,
    is_nullable,
    remarks,
    ci."mofId" as mofid,
    ci."lineageId" as lineageid
  from
    sys_boot.jdbc_metadata.columns_view_internal ci
  inner join
    sys_cwm."Relational"."SQLDataType" dt
  on
    ci."type" = dt."mofId"
;

grant select on dba_columns to public;

create view dba_views as
  select
    catalog_name,
    schema_name,
    view_name,
    ai."name" as creator,
    creation_timestamp,
    last_modification_timestamp,
    original_text,
    remarks,
    vi."mofId" as mofid,
    vi."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_views_internal2 vi
  inner join
    sys_fem."Security"."AuthId" ai
  on
    vi."Grantee" = ai."mofId"
;

grant select on dba_views to public;

create view dba_stored_tables as
  select
    catalog_name,
    schema_name,
    table_name,
    ai."name" as creator,
    creation_timestamp,
    last_modification_timestamp,
    last_analyze_row_count,
    last_analyze_timestamp,
    remarks,
    sti."mofId" as mofid,
    sti."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_stored_tables_internal2 sti
  inner join
    sys_fem."Security"."AuthId" ai
  on
    sti."Grantee" = ai."mofId"
;

grant select on dba_stored_tables to public;

create view dba_routines as
  select
    catalog_name,
    schema_name,
    invocation_name,
    specific_name,
    external_name,
    routine_type,
    ai."name" as creator,
    creation_timestamp,
    last_modified_timestamp,
    is_table_function,
    parameter_style,
    is_deterministic,
    data_access,
    remarks,
    ri."mofId" as mofid,
    ri."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_routines_internal2 ri
  inner join
    sys_fem."Security"."AuthId" ai
  on
    ri."Grantee" = ai."mofId"
;

grant select on dba_routines to public;

create view dba_routine_parameters as
  select
    catalog_name,
    schema_name,
    routine_specific_name,
    parameter_name,
    ordinal,
    case when rpi.is_table_function and parameter_name='RETURN' then 'TABLE'
         else dt."name" end as datatype,
    "PRECISION",
    dec_digits,
    remarks,
    rpi."mofId" as mofid,
    rpi."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_routine_parameters_internal1 rpi
  inner join
    sys_cwm."Relational"."SQLDataType" dt
  on
    rpi."type" = dt."mofId"
;

grant select on dba_routine_parameters to public;

create view dba_foreign_wrappers as
  select
    foreign_wrapper_name,
    library,
    "LANGUAGE",
    ai."name" as creator,
    creation_timestamp,
    last_modified_timestamp,
    remarks,
    fwi."mofId" as mofid,
    fwi."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_foreign_wrappers_internal fwi
  inner join
    sys_fem."Security"."AuthId" ai
  on
    fwi."Grantee" = ai."mofId"
;

grant select on dba_foreign_wrappers to public;

create view dba_foreign_wrapper_options as 
  select
    dw."name" as foreign_wrapper_name,
    so."name" as option_name,
    so."value" as option_value,
    so."mofId" as mofid
  from
    sys_fem.med."DataWrapper" dw
  inner join
    sys_fem.med."StorageOption" so
  on
    dw."mofId" = so."StoredElement"
  where 
    dw."foreign" = true
;

grant select on dba_foreign_wrapper_options to public;

create view dba_foreign_servers as
  select
    fsi.foreign_wrapper_name,
    fsi.foreign_server_name,
    ai."name" as creator,
    fsi.creation_timestamp,
    fsi.last_modified_timestamp,
    fsi.remarks,
    fsi."mofId" as mofid,
    fsi."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_foreign_servers_internal2 fsi
  inner join
    sys_fem."Security"."AuthId" ai
  on
    fsi."Grantee" = ai."mofId"
;

grant select on dba_foreign_servers to public;

create view dba_foreign_server_options as
  select
    foreign_wrapper_name,
    foreign_server_name,
    so."name" as option_name,
    so."value" as option_value,
    so."mofId" as mofid
  from
    sys_boot.mgmt.dba_foreign_servers_internal1 fsi
  inner join
    sys_fem.med."StorageOption" so
  on
    fsi."mofId" = so."StoredElement"
;

grant select on dba_foreign_server_options to public;

create view dba_foreign_tables as
  select
    fti.foreign_wrapper_name,
    fti.foreign_server_name,
    fti.foreign_table_name,
    ai."name" as creator,
    fti.creation_timestamp,
    fti.last_modified_timestamp,
    fti.last_analyze_row_count,
    fti.last_analyze_timestamp,
    fti.remarks,
    fti."mofId" as mofid,
    fti."lineageId" as lineageid
  from
    sys_boot.mgmt.dba_foreign_tables_internal2 fti
  inner join
    sys_fem."Security"."AuthId" ai
  on
    fti."Grantee" = ai."mofId"
;

grant select on dba_foreign_tables to public;

create view dba_foreign_table_options as
  select
    foreign_wrapper_name,
    foreign_server_name,
    foreign_table_name,
    so."name" as option_name,
    so."value" as option_value,
    so."mofId" as mofid
  from
    sys_boot.mgmt.dba_foreign_tables_internal1 fti
  inner join
    sys_fem.med."StorageOption" so
  on
    fti."mofId" = so."StoredElement"
;

grant select on dba_foreign_table_options to public;


-- Export schema to csv files UDP
create procedure export_schema_to_csv(
  in cat varchar(128),
  in schma varchar(128),
  in exclude boolean, 
  in tlist varchar(65535),
  in tpattern varchar(65535),
  in dir varchar(65535),
  in bcp boolean) 
language java
reads sql data
called on null input
external name 'class net.sf.farrago.syslib.FarragoExportSchemaUDR.exportSchemaToCsv';

create procedure export_foreign_schema_to_csv(
  in serv varchar(128),
  in fschema varchar(128),
  in exclude boolean,
  in tlist varchar(65535),
  in tpattern varchar(65535),
  in dir varchar(65535),
  in bcp boolean)
language java
modifies sql data
called on null input
external name 'class net.sf.farrago.syslib.FarragoExportSchemaUDR.exportForeignSchemaToCsv';