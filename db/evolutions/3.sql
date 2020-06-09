#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Question 
        add column name varchar(255);

# --- !Downs