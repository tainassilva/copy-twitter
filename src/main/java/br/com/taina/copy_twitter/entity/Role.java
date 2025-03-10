package br.com.taina.copy_twitter.entity;

import jakarta.persistence.*;

@Entity
@Table(name= "tb_roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="role_id")
    private Long roleId;

    private String name;

    public Role() {
    }

    public Role(Long roleId, String name) {
        this.roleId = roleId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", name='" + name + '\'' +
                '}';
    }

    public enum Values{

        ADMIN(1L),

        BASIC(2L);


        final Long roleIdd;

        Values(long roleIdd) {
            this.roleIdd = roleIdd;
        }
    }
}
