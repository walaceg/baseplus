package com.baseplus.modules.usuario.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import com.baseplus.modules.usuario.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    @Query("select u from Usuario u order by u.criadoEm desc, u.id desc")
    java.util.List<Usuario> findAllOrderByCriadoEmDesc();

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRoles_Id(Long roleId);

    long countByRoles_NameIgnoreCase(String roleName);

    @Query(
            value = """
                    select distinct u
                    from Usuario u
                    join u.roles r
                    where r.id = :roleId
                      and (
                        :search is null
                        or lower(u.nome) like concat('%', :search, '%')
                        or lower(u.email) like concat('%', :search, '%')
                        or lower(coalesce(u.nomeExibicao, '')) like concat('%', :search, '%')
                      )
                    """,
            countQuery = """
                    select count(distinct u.id)
                    from Usuario u
                    join u.roles r
                    where r.id = :roleId
                      and (
                        :search is null
                        or lower(u.nome) like concat('%', :search, '%')
                        or lower(u.email) like concat('%', :search, '%')
                        or lower(coalesce(u.nomeExibicao, '')) like concat('%', :search, '%')
                      )
                    """
    )
    Page<Usuario> findByRoleId(@Param("roleId") Long roleId, @Param("search") String search, Pageable pageable);

    @Query(
            value = """
                    select u
                    from Usuario u
                    where not exists (
                        select 1
                        from Usuario linked
                        join linked.roles r
                        where linked.id = u.id
                          and r.id = :roleId
                    )
                      and (
                        :search is null
                        or lower(u.nome) like concat('%', :search, '%')
                        or lower(u.email) like concat('%', :search, '%')
                        or lower(coalesce(u.nomeExibicao, '')) like concat('%', :search, '%')
                      )
                    """,
            countQuery = """
                    select count(u.id)
                    from Usuario u
                    where not exists (
                        select 1
                        from Usuario linked
                        join linked.roles r
                        where linked.id = u.id
                          and r.id = :roleId
                    )
                      and (
                        :search is null
                        or lower(u.nome) like concat('%', :search, '%')
                        or lower(u.email) like concat('%', :search, '%')
                        or lower(coalesce(u.nomeExibicao, '')) like concat('%', :search, '%')
                      )
                    """
    )
    Page<Usuario> findWithoutRoleId(@Param("roleId") Long roleId, @Param("search") String search, Pageable pageable);
}
