create table test (id number(19, 0),
   description varchar2(20 char),
   value_ts timestamp(0) default current_timestamp,
   primary key (id)
);

comment on table test is 'My test table';
comment on column test.description is 'My test.description column';
