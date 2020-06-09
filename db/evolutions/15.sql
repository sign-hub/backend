#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Media 
        add column parent_uuid varchar(255);

-- schema update # 2

    alter table Media 
        add index FK_n3vbd6ivsps2ri5bitdd291f2 (parent_uuid), 
        add constraint FK_n3vbd6ivsps2ri5bitdd291f2 
        foreign key (parent_uuid) 
        references Media (uuid);
        
# --- !Downs
