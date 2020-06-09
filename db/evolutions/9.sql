#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Option change column valuefield valuefield LONGTEXT NULL DEFAULT NULL;

-- schema update # 2

    alter table Media 
        add column isUser boolean;
        
-- schema update # 3

    update Media set isUser = false;
        
# --- !Downs
