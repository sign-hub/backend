#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Test 
        add column creationDate datetime;
        
# --- !Downs
