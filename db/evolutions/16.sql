#
# versione base database CINI
#

# --- !Ups
-- schema update # 1

    alter table Report 
        add column isComplessive boolean;

-- schema update # 2

    alter table Report 
        add column jsonContent longtext;

-- schema update # 3

    alter table Report 
        add column jsonFilePath varchar(255);
        
# --- !Downs
