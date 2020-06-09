#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table test_contentprovideruser (
        Test_uuid varchar(255) not null,
        contentProviderList_uuid varchar(255) not null
    );

-- schema update # 2

    create table test_editoruser (
        Test_uuid varchar(255) not null,
        editorList_uuid varchar(255) not null
    );

-- schema update # 3

    alter table test_contentprovideruser 
        add index FK_npsfe7shjaa7pviij2qiuiijt (contentProviderList_uuid), 
        add constraint FK_npsfe7shjaa7pviij2qiuiijt 
        foreign key (contentProviderList_uuid) 
        references EndUser (uuid);

-- schema update # 4

    alter table test_contentprovideruser 
        add index FK_906u30yyrm894n357qt0rjb0y (Test_uuid), 
        add constraint FK_906u30yyrm894n357qt0rjb0y 
        foreign key (Test_uuid) 
        references Test (uuid);

-- schema update # 5

    alter table test_editoruser 
        add index FK_16nb3du21wdo9retbpeaowfwv (editorList_uuid), 
        add constraint FK_16nb3du21wdo9retbpeaowfwv 
        foreign key (editorList_uuid) 
        references EndUser (uuid);

-- schema update # 6

    alter table test_editoruser 
        add index FK_8nds6ioi9onmpo5cdo2tvnimt (Test_uuid), 
        add constraint FK_8nds6ioi9onmpo5cdo2tvnimt 
        foreign key (Test_uuid) 
        references Test (uuid);
        
# --- !Downs
