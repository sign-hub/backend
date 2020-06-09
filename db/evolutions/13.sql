#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Media 
        add column question varchar(255);

-- schema update # 2

    alter table Media 
        add column report varchar(255);
        
# --- !Downs
