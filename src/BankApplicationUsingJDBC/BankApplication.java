package BankApplicationUsingJDBC;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class BankApplication {

	public static void main(String[] args) throws SQLException {

		com.mysql.cj.jdbc.Driver d = new com.mysql.cj.jdbc.Driver();
		DriverManager.registerDriver(d);

		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/db3", "root", "ashok");
		if (con != null) {
			Scanner sc = new Scanner(System.in);
			System.out.println("WELCOME TO BANK");
			int choice = 1;
			do {

				System.out.println("1.Add bank customer\n2.View all Customer\n3.update customer\n4.Delete customer\n"
						+ "5.Deposit Money\n6.Withdraw Money\n7.Self Transfer Money\n8.View Statement\n9.Check Bank Balance\n10.add bank");
				System.out.println("\nEnter your choice");
				int cho = sc.nextInt();

				switch (cho) {
				case 1:
					// Add bank customer

					int acno, balance, bn = 0;
					String cname, email, contact, address;
					PreparedStatement ps = con.prepareStatement("select bankid,bank_name from bank");
					ResultSet r1 = ps.executeQuery();
					while (r1.next()) {
						int b1 = r1.getInt("bankid");
						String n1 = r1.getString("bank_name");
						System.out.println(b1 + " : " + n1);
					}
					System.out.println("Select Bank");
					int cb = sc.nextInt();
					int flag = 0;
					r1 = ps.executeQuery();
					while (r1.next()) {
						int b1 = r1.getInt("bankid");
						if (b1 == cb) {
							flag = 1;
							bn = cb;
							break;
						}
					}
					if (flag != 0) {
						System.out.println("Enter new customer Details");
						System.out.println("Enter Customer_name,email,contact,address,balance");
						sc.nextLine();
						cname = sc.nextLine();
						email = sc.nextLine();
						contact = sc.nextLine();
						address = sc.nextLine();
						balance = sc.nextInt();

						ps = con.prepareStatement("insert into customer values(?,?,?,?,?,?)");
						ps.setInt(1, 0);
						ps.setString(2, cname);
						ps.setString(3, email);
						ps.setString(4, contact);
						ps.setString(5, address);
						ps.setInt(6, balance);

						int value = ps.executeUpdate();
						if (value != 0) {
							// when account create done
							System.out.println("Customer Added successfully");
							ps = con.prepareStatement("select account_no,cust_name,contact from customer");
							ResultSet r = ps.executeQuery();
							int an = 0;
							int flag2 = 0;
							while (r.next()) {
								int ano = r.getInt("account_no");
								String na = r.getString("cust_name");
								String cont = r.getString("contact");
								if (cname.compareTo(na) == 0 && contact.compareTo(cont) == 0) {
									flag2 = 1;
									an = ano;
									break;
								}

							}
							if (flag2 != 0) {
								// insert data in cbijoin table
								ps = con.prepareStatement("insert into cbijoin (account_no,bankid) values(?,?)");
								ps.setInt(1, an);
								ps.setInt(2, bn);
								int v2 = ps.executeUpdate();
							}

						} else {
							System.out.println("Customer Not Added");
						}
					} else
						System.out.println("Select correct bank");

					break;

				case 2:
					// View all Customer
					System.out.println("All Customer Details Here !");
					ps = con.prepareStatement("select * from customer");
					ResultSet re = ps.executeQuery();
					while (re.next()) {
						acno = re.getInt("account_no");
						cname = re.getString("cust_name");
						email = re.getString("email");
						contact = re.getString("contact");
						address = re.getString("address");
						balance = re.getInt("balance");
						System.out.println(
								acno + "\t" + cname + "\t" + email + "\t" + contact + "\t" + address + "\t" + balance);
					}
					break;

				case 3:
					// update customer
					System.out.println("Enter account no");
					acno = sc.nextInt();
					ps = con.prepareStatement(
							"Update customer set cust_name=? ,email=? ,contact=?, address=? where account_no=?");
					System.out.println("Enter new customer name, email, contact, address");
					sc.nextLine();
					cname = sc.nextLine();
					email = sc.nextLine();
					contact = sc.nextLine();
					address = sc.nextLine();

					ps.setString(1, cname);
					ps.setString(2, email);
					ps.setString(3, contact);
					ps.setString(4, address);
					ps.setInt(5, acno);
					int value = ps.executeUpdate();
					String s = (value > 0) ? "Update successfully" : "Not Update";
					System.out.println(s);
					break;

				case 4:
					// Delete customer
					System.out.println("Enter customer account no");
					acno = sc.nextInt();
					ps = con.prepareStatement("delete from customer where account_no=?");
					ps.setInt(1, acno);
					value = ps.executeUpdate();
					String s1 = (value > 0) ? "Customer Delete Successfully" : "Customer not delete";
					if (value > 0) {
						ps = con.prepareStatement("create trigger IF NOT Exists mini1 after delete on customer "
								+ "for each row " + "begin "
								+ "insert into deletedCust values(account_no,cust_name,email,contact,address,balance); "
								+ "end");
						int va = ps.executeUpdate();
					}
					System.out.println(s1);
					break;

				case 5:
					// Deposit Money
					System.out.println("Enter customer account no");
					acno = sc.nextInt();

					ps = con.prepareStatement("select account_no ,balance from customer where account_no=?");
					ps.setInt(1, acno);
					ResultSet se = ps.executeQuery();
					if (se.next()) {
						int ac = se.getInt("account_no");
						int bal = se.getInt("balance");
						System.out.println("Enter Deposit money");
						int Dmoney = sc.nextInt();
						balance = bal + Dmoney;
						ps = con.prepareStatement("Update customer set balance=? where account_no=?");
						ps.setInt(1, balance);
						ps.setInt(2, ac);
						int v1 = ps.executeUpdate();
						if (v1 > 0) {
							ps = con.prepareStatement("insert into transaction values(?,?,?,?,?,?,?)");
							ps.setInt(1, 0);
							ps.setInt(2, ac);
							java.sql.Date date = new java.sql.Date((new java.util.Date()).getTime());
							ps.setDate(3, date);
							ps.setInt(4, Dmoney);
							ps.setInt(5, bal);
							ps.setInt(6, balance);
							ps.setInt(7, 1);
							int v3 = ps.executeUpdate();
							if (v3 > 0) {
								int bid = 0;
								// we fetch bank id using account no
								PreparedStatement psd = con.prepareStatement(
										"select b.bankid from customer c inner join cbijoin cb on cb.account_no=c.account_no inner join bank b on b.bankid=cb.bankid where c.account_no=?");
								psd.setInt(1, ac);
								ResultSet rs1 = psd.executeQuery();
								if (rs1.next()) {
									bid = rs1.getInt("bankid");
								}
								ps = con.prepareStatement("insert into cbijoin(account_no,bankid,tsid) values (?,?,?)");
								ps.setInt(1, ac);
								ps.setInt(2, bid);
								ps.setInt(3, 1);
								int v6 = ps.executeUpdate();
								if (v6 > 0) {
									System.out.println("Transaction successfully");
								}
							}

						} else {
							System.out.println("Transaction failed");
						}

					} else {
						System.out.println("Customer account not found");
					}

					break;

				case 6:
					// Withdraw Money

					System.out.println("Enter account no");
					acno = sc.nextInt();

					ps = con.prepareStatement("select account_no,balance from customer where account_no=?");
					ps.setInt(1, acno);
					se = ps.executeQuery();
					if (se.next()) {
						int ac = se.getInt("account_no");
						int bal = se.getInt("balance");
						System.out.println("Enter Withdraw money");
						int Wmoney = sc.nextInt();

						if (Wmoney <= bal) {
							balance = bal - Wmoney;
							ps = con.prepareStatement("Update customer set balance=? where account_no=?");
							ps.setInt(1, balance);
							ps.setInt(2, ac);
							int v1 = ps.executeUpdate();
							if (v1 > 0) {
								ps = con.prepareStatement("insert into transaction values(?,?,?,?,?,?,?)");
								ps.setInt(1, 0);
								ps.setInt(2, ac);
								java.sql.Date date = new java.sql.Date((new java.util.Date()).getTime());
								ps.setDate(3, date);
								ps.setInt(4, Wmoney);
								ps.setInt(5, bal);
								ps.setInt(6, balance);
								ps.setInt(7, 2);
								int v3 = ps.executeUpdate();
								if (v3 > 0) {
									int bid = 0;
									// we fetch bank id using account no
									PreparedStatement psd = con.prepareStatement(
											"select b.bankid from customer c inner join cbijoin cb on cb.account_no=c.account_no inner join bank b on b.bankid=cb.bankid where c.account_no=?");
									psd.setInt(1, ac);
									ResultSet rs1 = psd.executeQuery();
									if (rs1.next()) {
										bid = rs1.getInt("bankid");
									}
									ps = con.prepareStatement(
											"insert into cbijoin(account_no,bankid,tsid) values (?,?,?)");
									ps.setInt(1, ac);
									ps.setInt(2, bid);
									ps.setInt(3, 2);
									int v6 = ps.executeUpdate();
									if (v6 > 0) {
										System.out.println("Transaction successfully");
									}
								}

							}
						} else {
							System.out.println("Transaction failed");
						}

					} else {
						System.out.println("Customer account not found");
					}
					break;

				case 7:
					// Self Transfer Money
					System.out.println("Enter Your Bank account no");
					acno = sc.nextInt();
					ps = con.prepareStatement("select account_no,balance from customer where account_no=?");
					ps.setInt(1, acno);
					ResultSet re1 = ps.executeQuery();
					if (re1.next()) {
						int ano = re1.getInt("account_no");
						int bal = re1.getInt("balance");
						System.out.println("Enter Amount");
						int Dmoney = sc.nextInt();
						if (Dmoney <= bal) {
							System.out.println("Enter another customer account no");
							int acountno = sc.nextInt();
							PreparedStatement ps1 = con
									.prepareStatement("select account_no,balance from customer where account_no=?");
							ps1.setInt(1, acountno);
							ResultSet re2 = ps1.executeQuery();
							if (re2.next()) {
								int another_acno = re2.getInt("account_no");
								int another_bal = re2.getInt("balance");

								// update balance another customer
								int Balance2 = another_bal + Dmoney;
								PreparedStatement ps3 = con
										.prepareStatement("Update customer set balance=? where account_no=?");

								ps3.setInt(1, Balance2);
								ps3.setInt(2, another_acno);
								int v = ps3.executeUpdate();

								// update balance customer
								ps3 = con.prepareStatement("Update customer set balance=? where account_no=?");
								int Balance1 = bal - Dmoney; // 14000-1000
								ps3.setInt(1, Balance1);
								ps3.setInt(2, ano);
								int v2 = ps3.executeUpdate();

								if (v2 > 0) {
									// insert transaction withdraw record info
									ps = con.prepareStatement("insert into transaction values(?,?,?,?,?,?,?)");
									ps.setInt(1, 0);
									ps.setInt(2, ano);
									java.sql.Date date = new java.sql.Date((new java.util.Date()).getTime());
									ps.setDate(3, date);
									ps.setInt(4, Dmoney);
									ps.setInt(5, bal);
									ps.setInt(6, Balance1);
									ps.setInt(7, 2);
									int v3 = ps.executeUpdate();

									int bid = 0;
									// we fetch bank id using account no
									PreparedStatement psd = con.prepareStatement(
											"select b.bankid from customer c inner join cbijoin cb on cb.account_no=c.account_no inner join bank b on b.bankid=cb.bankid where c.account_no=?");
									psd.setInt(1, ano);
									ResultSet rs1 = psd.executeQuery();
									if (rs1.next()) {
										bid = rs1.getInt("bankid");
									}
									ps = con.prepareStatement(
											"insert into cbijoin(account_no,bankid,tsid) values (?,?,?)");
									ps.setInt(1, ano);
									ps.setInt(2, bid);
									ps.setInt(3, 2);
									int v6 = ps.executeUpdate();

									// insert another account transaction deposit info
									ps = con.prepareStatement("insert into transaction values(?,?,?,?,?,?,?)");
									ps.setInt(1, 0);
									ps.setInt(2, another_acno);
									java.sql.Date date1 = new java.sql.Date((new java.util.Date()).getTime());
									ps.setDate(3, date1);
									ps.setInt(4, Dmoney);
									ps.setInt(5, another_bal);
									ps.setInt(6, Balance2);
									ps.setInt(7, 1);
									v3 = ps.executeUpdate();
									bid = 0;
									// we fetch bank id using account no
									psd = con.prepareStatement(
											"select b.bankid from customer c inner join cbijoin cb on cb.account_no=c.account_no inner join bank b on b.bankid=cb.bankid where c.account_no=?");
									psd.setInt(1, another_acno);
									rs1 = psd.executeQuery();
									if (rs1.next()) {
										bid = rs1.getInt("bankid");
									}
									ps = con.prepareStatement(
											"insert into cbijoin(account_no,bankid,tsid) values (?,?,?)");
									ps.setInt(1, another_acno);
									ps.setInt(2, bid);
									ps.setInt(3, 1);
									v6 = ps.executeUpdate();

									System.out.println("Amount Transfer successfully");
								} else
									System.out.println("Failed transaction");
							} else
								System.out.println("Another customer account not found");

						} else
							System.out.println("Insufficient balance");
					} else {
						System.out.println("Customer Account Not Found");
					}

					break;

				case 8:
					// View Statement
					System.out.println("Enter bank account no");
					acno = sc.nextInt();
					ps = con.prepareStatement("select account_no from customer where account_no=?");
					ps.setInt(1, acno);
					ResultSet se1 = ps.executeQuery();
					if (se1.next()) {
						ps = con.prepareStatement(
								"select c.account_no,c.cust_name,b.bank_name,b.IFSC_code,c.address,c.balance from customer c "
										+ "inner join cbijoin cb on cb.account_no=c.account_no inner join bank b on cb.bankid=b.bankid "
										+ "where c.account_no=?");

						ps.setInt(1, acno);
						ResultSet fe = ps.executeQuery();
						int bal = 0;
						int flag2 = 0;
						if (fe.next()) {
							System.out.println(
									"----------------------------------------------------------------------------------");
							flag2 = 1;
							int cacno = fe.getInt("account_no");
							cname = fe.getString("cust_name");
							String bankname = fe.getString("bank_name");
							String ifsc_code = fe.getString("IFSC_code");
							address = fe.getString("address");
							balance = fe.getInt("balance");
							bal = balance;
							System.out.println("Bank :" + bankname + "\nAccount No :" + cacno + "\nName :" + cname
									+ "\naddress :" + address + "\nIFSC Code :" + ifsc_code);
							System.out.println("\nTransation Details");

						}
						PreparedStatement ps3 = con.prepareStatement(
								"select t.tid ,ts.transname,t.date,t.rupees,t.afterBalance from transaction t  inner join transactionStatement ts on ts.tsid=t.tsid where account_no=? ");
						ps3.setInt(1, acno);
						fe = ps3.executeQuery();

						while (fe.next()) {
							int tid=fe.getInt("tid");
							String transname = fe.getString("transname");
							Date date = fe.getDate("date");
							int rupees = fe.getInt("rupees");
							int afterbalance = fe.getInt("afterBalance");
							System.out.println("Id :"+tid+ "\tDate :" + date + "\t"+transname +" :" + rupees + "\tafter balance :" + afterbalance);
						}
						if (flag2 != 0) {
							System.out.println(
									"----------------------------------------------------------------------------------");
							System.out.println("Availabe Balance :" + bal);
						}
					} else {
						System.out.println("Customer not found");
					}

					break;

				case 9:
					// Check Bank Balance
					System.out.println("Enter Customer Account no");
					acno = sc.nextInt();
					ps = con.prepareStatement(
							"select c.cust_name,c.balance,b.bank_name from customer c inner join cbijoin cb on cb.account_no=c.account_no inner join bank b on b.bankid=cb.bankid where c.account_no=?");
					ps.setInt(1, acno);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						String name = rs.getString("cust_name");
						balance = rs.getInt("balance");
						String bankName = rs.getString("bank_name");
						System.out.println("-------------------------------------------------------------");
						System.out.println("Bank account balance fetched successfully");
						System.out.println(bankName);
						System.out.println("Customer Name = " + name);
						System.out.println("Available Balance = " + balance);
						System.out.println("-------------------------------------------------------------");
					} else {
						System.out.println("Customer not account found");
					}
					break;

				case 10:
					// Add new bank
					System.out.println("Enter new bank name");
					sc.nextLine();
					String bankName = sc.nextLine();
					System.out.println("Enter IFSC Code");
					String IFSC = sc.nextLine();
					ps = con.prepareStatement("insert into bank values(?,?,?)");
					ps.setInt(1, 0);
					ps.setString(2, bankName);
					ps.setString(3, IFSC);
					int msg = ps.executeUpdate();
					if (msg > 0) {
						System.out.println("Bank Added Successfully");
					} else
						System.out.println("Bank Not Added");
					break;

				default:
					System.out.println("Wrong choice");

				}
				System.out.println("\n( 1.continue / 0.stop )");
				choice = sc.nextInt();

			} while (choice != 0);
			sc.close();
		}
		con.close();

	}

}
