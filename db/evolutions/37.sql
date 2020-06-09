#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table EsAccount 
        add constraint UC_esaccount 
        unique (username);