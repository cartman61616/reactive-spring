package com.mumbles.bootstrap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

public class BaseCustomerService implements CustomerService{

  private final RowMapper<Customer> rowMapper = (rs, i) -> new Customer(rs.getLong("id"), rs.getString("NAME"));

  private final JdbcTemplate jdbcTemplate;

  protected BaseCustomerService(DataSource ds) {
    this.jdbcTemplate = new JdbcTemplate(ds);
  }
  @Override
  public Collection<Customer> save(String... names) {
    var customerList = new ArrayList<Customer>();
    for (String name : names) {
      var keyHolder = new GeneratedKeyHolder();
      this.jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(
            "insert into CUSTOMERS (name) values ?",
            Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        return ps;
      }, keyHolder);
      var keyHolderKey = Objects.requireNonNull(keyHolder.getKey()).longValue();
      var customer = this.findById(keyHolderKey);
      Assert.notNull(name, "the name given must not be null");
      customerList.add(customer);
    }
    return customerList;
  }

  @Override
  public Customer findById(Long id) {
    var sql = "select * from CUSTOMERS where id = ?";
    return this.jdbcTemplate.queryForObject(sql, this.rowMapper, id);
  }

  @Override
  public Collection<Customer> findAll() {
    return this.jdbcTemplate.query("select * from CUSTOMERS", rowMapper);
  }
}
