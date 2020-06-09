#
# versione base database CINI
#

# --- !Ups
-- schema update # 1

    alter table Report 
        add column workerId varchar(255);
        
# --- !Downs
