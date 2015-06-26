create view eurovoc_view as
select d.eurovoc_theme, md.eurovoc_microtheme, count(*) c from 
(select upper(value) eurovoc_theme, package_id from package_extra where state = 'active' and key = 'eurovoc-domain') as d 
join 
(select upper(value) eurovoc_microtheme, package_id from package_extra where state = 'active' and key = 'eurovoc-microdomain') as md 
on d.package_id = md.package_id
group by eurovoc_theme, eurovoc_microtheme order by c desc;