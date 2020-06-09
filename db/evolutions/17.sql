#
# versione base database CINI
#

# --- !Ups
-- schema update # 1

    alter table Report 
        add column languageName varchar(255);
        
# --- !Downs
