/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;
import koneksi.koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;



/**
 *
 * @author ASUS
 */
public class FSuplemen extends javax.swing.JFrame {
Connection connection;
DefaultTableModel model;
    /**
     * Creates new form FrameKelas
     */
    public FSuplemen() {
        initComponents();
        connection = koneksi.getConnection();
        tKode.requestFocus();
        getDataTable();
        loadJenisToComboBox();
        loadSupplierToComboBox();
    }
    
   private void getDataTable() {
    model = (DefaultTableModel) table.getModel();
    model.setRowCount(0);
    try {
        Statement stat = connection.createStatement();
        String sql = "SELECT s.kode, s.nama_barang, s.keterangan, s.harga, s.stok, " +
                     "j.jenis, sp.nama_supplier " +
                     "FROM suplemen s " +
                     "JOIN jenis j ON s.kode_jenis = j.kode_jenis " +
                     "JOIN supplier sp ON s.kode_supp = sp.kode_supp " +  // <== PASTIKAN ADA SPASI DI SINI
                     "ORDER BY s.kode";  // <== dan ini adalah baris ORDER BY
        ResultSet res = stat.executeQuery(sql);
        while (res.next()) {
            Object[] obj = new Object[7];
            obj[0] = res.getString("kode");
            obj[1] = res.getString("nama_barang");
            obj[2] = res.getString("keterangan");
            obj[3] = res.getInt("harga");
            obj[4] = res.getInt("stok");
            obj[5] = res.getString("jenis");
            obj[6] = res.getString("nama_supplier");
            model.addRow(obj);
        }
    } catch (SQLException err) {
        err.printStackTrace();
    }
}




    private void refresh() {
    model = (DefaultTableModel) table.getModel();
    model.setRowCount(0);
    getDataTable();
}

    private void reset() {
    tKode.setText("");
    tNama.setText("");
    tKet.setText("");
    tHarga.setText("");
    tStok.setText("");
    bSimpan.setEnabled(true);
    tKode.setEditable(true);
    tKode.requestFocus(true);
}
    
   private void insert() {
    PreparedStatement statement = null;
    String sql = "INSERT INTO suplemen (kode, nama_barang, keterangan, harga, stok, kode_jenis, kode_supp) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try {
        // Ambil ID dari ComboBox
        String selectedJenis = cbJenis.getSelectedItem().toString();
        String kode_jenis = selectedJenis.split(" - ")[0]; // Ambil ID sebelum ' - '

        String selectedSupplier = cbSupplier.getSelectedItem().toString();
        String kode_supp = selectedSupplier.split(" - ")[0];

        // Siapkan statement
        statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, tKode.getText());
        statement.setString(2, tNama.getText());
        statement.setString(3, tKet.getText());
        statement.setInt(4, Integer.parseInt(tHarga.getText()));
        statement.setInt(5, Integer.parseInt(tStok.getText()));
        statement.setInt(6, Integer.parseInt(kode_jenis));
        statement.setInt(7, Integer.parseInt(kode_supp));

        statement.executeUpdate();
        JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan!");
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal menambahkan data: " + ex.getMessage());
    } finally {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}


    
    private void update() {
    PreparedStatement statement = null;
    String sql = "UPDATE suplemen SET nama_barang=?, keterangan=?, harga=?, stok=?, kode_jenis=?, kode_supp=? "
               + "WHERE kode=?";
    try {
        // Ambil nilai id dari ComboBox
        String selectedJenis = cbJenis.getSelectedItem().toString();
        String kode_jenis = selectedJenis.split(" - ")[0]; // ambil ID saja

        String selectedSupplier = cbSupplier.getSelectedItem().toString();
        String kode_supp = selectedSupplier.split(" - ")[0];

        // Siapkan statement
        statement = connection.prepareStatement(sql);
        statement.setString(1, tNama.getText());
        statement.setString(2, tKet.getText());
        statement.setInt(3, Integer.parseInt(tHarga.getText()));
        statement.setInt(4, Integer.parseInt(tStok.getText()));
        statement.setInt(5, Integer.parseInt(kode_jenis));
        statement.setInt(6, Integer.parseInt(kode_supp));
        statement.setString(7, tKode.getText());

        statement.executeUpdate();
        JOptionPane.showMessageDialog(null, "Data berhasil diupdate!");
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal mengupdate data: " + ex.getMessage());
    } finally {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}



private void delete() {
    PreparedStatement statement = null;
    String sql = "DELETE FROM suplemen WHERE kode=?";
    try {
        statement = connection.prepareStatement(sql);
        statement.setString(1, tKode.getText());
        statement.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}


private void search() {
    model = (DefaultTableModel) table.getModel();
    PreparedStatement statement = null;
    try {
        model.setRowCount(0);
        String sql;

        // Cek apakah field pencarian kosong
        if (tCari.getText().trim().isEmpty()) {
            // Tampilkan semua data
            sql = "SELECT s.kode, s.nama_barang, s.keterangan, s.harga, s.stok, " +
                  "j.jenis, sp.nama_supplier " +
                  "FROM suplemen s " +
                  "JOIN jenis j ON s.kode_jenis = j.kode_jenis " +
                  "JOIN supplier sp ON s.kode_supp = sp.kode_supp";
            statement = connection.prepareStatement(sql);
        } else {
            // Tampilkan data dengan filter pencarian
            sql = "SELECT s.kode, s.nama_barang, s.keterangan, s.harga, s.stok, " +
                  "j.jenis, sp.nama_supplier " +
                  "FROM suplemen s " +
                  "JOIN jenis j ON s.kode_jenis = j.kode_jenis " +
                  "JOIN supplier sp ON s.kode_supp = sp.kode_supp " +
                  "WHERE s.nama_barang LIKE ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + tCari.getText().trim() + "%");
        }

        ResultSet res = statement.executeQuery();
        while (res.next()) {
            Object[] obj = new Object[7];
            obj[0] = res.getString("kode");
            obj[1] = res.getString("nama_barang");
            obj[2] = res.getString("keterangan");
            obj[3] = res.getInt("harga");
            obj[4] = res.getInt("stok");
            obj[5] = res.getString("jenis");           // nama jenis, bukan kode
            obj[6] = res.getString("nama_supplier");   // nama supplier, bukan kode
            model.addRow(obj);
        }
    } catch (SQLException err) {
        err.printStackTrace();
    }
}


private void loadJenisToComboBox() {
    cbJenis.removeAllItems(); // Hapus semua isi dulu
    cbJenis.addItem("Pilih Jenis");
    try {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT kode_jenis, jenis FROM jenis");
        while (rs.next()) {
            cbJenis.addItem(rs.getInt("kode_jenis") + " - " + rs.getString("jenis"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal load data jenis");
    }
}



private void loadSupplierToComboBox() {
    cbSupplier.removeAllItems();
    cbSupplier.addItem("Pilih Supplier");
    try {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT kode_supp, nama_supplier FROM supplier");
        while (rs.next()) {
            cbSupplier.addItem(rs.getInt("kode_supp") + " - " + rs.getString("nama_supplier"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal load data supplier");
    }
}




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tKode = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tNama = new javax.swing.JTextField();
        tKet = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        bSimpan = new javax.swing.JButton();
        bUbah = new javax.swing.JButton();
        bHapus = new javax.swing.JButton();
        bReset = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        tHarga = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tStok = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        cbJenis = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        cbSupplier = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tCari = new javax.swing.JTextField();
        bRefresh = new javax.swing.JButton();
        bCari = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Kelola Data Suplemen");
        setBackground(new java.awt.Color(251, 249, 249));

        jPanel1.setBackground(new java.awt.Color(251, 249, 249));

        jLabel2.setText("Kode");

        tKode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tKodeActionPerformed(evt);
            }
        });

        jLabel3.setText("Nama Barang");

        tNama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tNamaActionPerformed(evt);
            }
        });

        jLabel4.setText("Keterangan");

        bSimpan.setText("Simpan");
        bSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSimpanActionPerformed(evt);
            }
        });

        bUbah.setText("Ubah");
        bUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bUbahActionPerformed(evt);
            }
        });

        bHapus.setText("Hapus");
        bHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHapusActionPerformed(evt);
            }
        });

        bReset.setText("Reset");
        bReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bResetActionPerformed(evt);
            }
        });

        jLabel6.setText("Harga");

        jLabel7.setText("Stok");

        jLabel8.setText("Jenis");

        cbJenis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel9.setText("Supplier");

        cbSupplier.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jPanel2.setBackground(new java.awt.Color(251, 249, 249));

        jLabel5.setText("Cari Data");

        tCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tCariActionPerformed(evt);
            }
        });

        bRefresh.setText("Refresh");
        bRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRefreshActionPerformed(evt);
            }
        });

        bCari.setText("Cari");
        bCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCariActionPerformed(evt);
            }
        });

        jScrollPane1.setBackground(new java.awt.Color(204, 204, 255));

        table.setBackground(new java.awt.Color(204, 204, 255));
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Kode", "Nama Barang", "Keterangan", "Harga", "Stok", "Jenis", "Supplier"
            }
        ));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 57, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(tCari, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(bCari)
                        .addGap(18, 18, 18)
                        .addComponent(bRefresh)
                        .addGap(132, 132, 132))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bRefresh)
                    .addComponent(bCari)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel1.setText("Kelola Data Suplemen");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tKode, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tKet, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addGap(18, 18, 18)
                                    .addComponent(tNama, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(89, 89, 89)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(bSimpan)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bUbah, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(bHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bReset, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tStok, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbJenis, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(tKode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(tNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tKet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(tHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(86, 86, 86))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(tStok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cbJenis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bSimpan)
                    .addComponent(bUbah))
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bReset)
                    .addComponent(bHapus))
                .addGap(27, 27, 27)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(179, 179, 179))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(597, 597, 597))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tKodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tKodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tKodeActionPerformed

    private void bUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUbahActionPerformed
        // TODO add your handling code here:
       if (tKode.getText().trim().isEmpty()) {
    JOptionPane.showMessageDialog(this, 
        "Pilih data suplemen yang akan diubah terlebih dahulu!", 
        "Notifikasi", JOptionPane.WARNING_MESSAGE);
} else if (
    tNama.getText().trim().isEmpty() || 
    tKet.getText().trim().isEmpty() || 
    tHarga.getText().trim().isEmpty() || 
    tStok.getText().trim().isEmpty()
) {
    JOptionPane.showMessageDialog(this, 
        "Lengkapi semua form terlebih dahulu!", 
        "Notifikasi", JOptionPane.WARNING_MESSAGE);
} else {
    update();                  // Panggil method update()
    refresh();                 // Refresh tabel
    reset();                   // Kosongkan form input
    bSimpan.setEnabled(true);  // Aktifkan tombol simpan lagi
    JOptionPane.showMessageDialog(this, 
        "Data Suplemen berhasil diubah", 
        "Notifikasi", JOptionPane.INFORMATION_MESSAGE);
}

    }//GEN-LAST:event_bUbahActionPerformed

    private void bHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHapusActionPerformed
        // TODO add your handling code here:
        if (!tKode.getText().trim().isEmpty()) {
    int alert = JOptionPane.showConfirmDialog(this, 
        "Anda yakin ingin menghapus data Suplemen ini?", 
        "Notifikasi", JOptionPane.YES_NO_OPTION, 
        JOptionPane.QUESTION_MESSAGE);

    if (alert == JOptionPane.YES_OPTION) {
        delete();           // Panggil method delete()
        refresh();          // Muat ulang data tabel
        reset();            // Kosongkan form
        bSimpan.setEnabled(true); // Aktifkan kembali tombol simpan
        JOptionPane.showMessageDialog(this, 
            "Data Suplemen berhasil dihapus", 
            "Notifikasi", JOptionPane.INFORMATION_MESSAGE);
    }
} else {
    JOptionPane.showMessageDialog(this, 
        "Pilih data suplemen terlebih dahulu!", 
        "Notifikasi", JOptionPane.WARNING_MESSAGE);
}

    }//GEN-LAST:event_bHapusActionPerformed

    private void bResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bResetActionPerformed
        // TODO add your handling code here:
        reset();
    }//GEN-LAST:event_bResetActionPerformed

    private void tNamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNamaActionPerformed

    private void bSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSimpanActionPerformed
        // TODO add your handling code here:
       if (
    tKode.getText().trim().isEmpty() || 
    tNama.getText().trim().isEmpty() || 
    tKet.getText().trim().isEmpty() || 
    tHarga.getText().trim().isEmpty() || 
    tStok.getText().trim().isEmpty()
) {
    JOptionPane.showMessageDialog(this, 
        "Lengkapi semua form terlebih dahulu!", 
        "Notifikasi", JOptionPane.WARNING_MESSAGE);
} else {
    insert();                 // Simpan data ke DB
    refresh();                // Tampilkan data terbaru di tabel
    reset();                  // Kosongkan form
    bSimpan.setEnabled(true); // Pastikan tombol tetap aktif
    JOptionPane.showMessageDialog(this, 
        "Data Suplemen berhasil ditambahkan", 
        "Notifikasi", JOptionPane.INFORMATION_MESSAGE);
}

    }//GEN-LAST:event_bSimpanActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        // TODO add your handling code here:
        tKode.setText(table.getModel().getValueAt(table.getSelectedRow(), 0).toString());
        tNama.setText(table.getModel().getValueAt(table.getSelectedRow(), 1).toString());
        tKet.setText(table.getModel().getValueAt(table.getSelectedRow(), 2).toString());
        tHarga.setText(table.getModel().getValueAt(table.getSelectedRow(), 3).toString());
        tStok.setText(table.getModel().getValueAt(table.getSelectedRow(), 4).toString());
        String jenisFromTable = table.getModel().getValueAt(table.getSelectedRow(), 5).toString();
for (int i = 0; i < cbJenis.getItemCount(); i++) {
    String item = cbJenis.getItemAt(i);
    if (item.contains(jenisFromTable)) {
        cbJenis.setSelectedIndex(i);
        break;
    }
}
        String suppFromTable = table.getModel().getValueAt(table.getSelectedRow(), 6).toString();
for (int i = 0; i < cbSupplier.getItemCount(); i++) {
    String item = cbSupplier.getItemAt(i);
    if (item.contains(suppFromTable)) {
        cbSupplier.setSelectedIndex(i);
        break;
    }
}

        tKode.setEditable(false);   // ID tidak boleh diubah
        bSimpan.setEnabled(false);  // Nonaktifkan tombol simpan agar tidak dobel insert

    }//GEN-LAST:event_tableMouseClicked

    private void bCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCariActionPerformed
        // TODO add your handling code here:
        search();
    }//GEN-LAST:event_bCariActionPerformed

    private void bRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRefreshActionPerformed
        // TODO add your handling code here:
        refresh();
    }//GEN-LAST:event_bRefreshActionPerformed

    private void tCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tCariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tCariActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FSuplemen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FSuplemen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FSuplemen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FSuplemen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FSuplemen().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bCari;
    private javax.swing.JButton bHapus;
    private javax.swing.JButton bRefresh;
    private javax.swing.JButton bReset;
    private javax.swing.JButton bSimpan;
    private javax.swing.JButton bUbah;
    private javax.swing.JComboBox<String> cbJenis;
    private javax.swing.JComboBox<String> cbSupplier;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField tCari;
    private javax.swing.JTextField tHarga;
    private javax.swing.JTextField tKet;
    private javax.swing.JTextField tKode;
    private javax.swing.JTextField tNama;
    private javax.swing.JTextField tStok;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
