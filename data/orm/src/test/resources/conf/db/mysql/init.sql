drop PROCEDURE if EXISTS t_execute_sp;
drop PROCEDURE if EXISTS t_query_sp;

delimiter //

create PROCEDURE t_execute_sp(in firstName VARCHAR(100))
BEGIN

    update owners set last_name = last_name where first_name = firstName;

END//

create PROCEDURE t_query_sp(in firstName varchar(100))
BEGIN

select * from owners where first_name = firstName;

end//

delimiter ;