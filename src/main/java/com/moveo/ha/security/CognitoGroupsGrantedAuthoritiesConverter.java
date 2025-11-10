package com.moveo.ha.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class CognitoGroupsGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String groupsClaimName;

    CognitoGroupsGrantedAuthoritiesConverter(String groupsClaimName) {
        this.groupsClaimName = groupsClaimName;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var groups = jwt.getClaimAsStringList(groupsClaimName);
        if (groups == null) groups = List.of();

        return groups.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}
