@if(ftype == null || ftype == 'D')
select
      dir_id id,       parent_id parentId,          principal_id principalId,        principal_Type principalType,        dir_name name,       full_path fullPath,
      '' extension,        0 size,          updated_at modifiedTime,         'D' ftype,       version,         is_deleted inTrash,
      null cvtState,
      0 locked,
      '' lockedBy,
      '' lockedByName,
      null lockedAt,
      not exists(select 1 from pan_dir where parent_id=d.dir_id and is_deleted =0) as leaf, @if(inOpen==null) #{adminPerm} @else #{openPerm} @endif perm
   from pan_dir d
   where @if(parentId==null) is_public = 1 @else parent_id = #{parentId} @endif @if(inOpen==null) AND principal_id = #{curId} @else AND principal_id = #{principalId} AND is_open=1 @endif AND is_deleted = 0
@endif

@if(ftype == null)
union
@endif

@if(ftype == null || ftype == 'F')
select
      file_id id,          dir_id parentId,         principal_id principalId,        principal_Type principalType,        file_name name,          full_path fullPath,
      file_ext extension,          file_size size,          updated_at modifiedTime,         'F' ftype,       version,         f.is_deleted inTrash,
      cvt_state cvtState,
      locked,
      locked_by lockedBy,
      if(name is null or name ='',full_name,name) lockedByName,
      locked_at lockedAt,
      1 as leaf, @if(inOpen==null) 65535 @else #{openPerm} @endif perm
   from pan_file f
   left join uam_user u
   on u.user_id = f.locked_by
   where dir_id = #{parentId} @if(inOpen==null) AND principal_id = #{curId} @else AND principal_id = #{principalId} AND is_open=1 @endif AND f.is_deleted = 0
@endif

@if(orderSql!=null and orderSql!='') $orderSql$ @endif