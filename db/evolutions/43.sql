#
# versione base database CINI
#

# --- !Ups
-- schema update # 1

    alter table Grammar 
        add column editedBy longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL ;