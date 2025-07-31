/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import koneksi.koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ASUS
 */
public class FBeli extends javax.swing.JFrame {

    Connection connection;
    DefaultTableModel model;
    /**
     * Creates new form FrameKelas
     */
    public FBeli() {
    initComponents();
    connection = koneksi.getConnection();
    getDataTable();
    cbBarang();
    addJumlahListener();
    autoNumber();
    tKode.requestFocus();
   cbNamaBarang.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            supp(); 
            loadHargaDanSupplier();
        }
    });

}

    
    private void supp() {
    try {
        String namaBarang = cbNamaBarang.getSelectedItem().toString();
        Connection con = koneksi.getConnection();
        Statement st = con.createStatement();
        String sql = "SELECT s.kode_supp, sp.nama_supplier " +
                     "FROM suplemen s " +
                     "JOIN supplier sp ON s.kode_supp = sp.kode_supp " +
                     "WHERE s.nama_barang = '" + namaBarang + "'";
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            tSupp.setText(rs.getString("nama_supplier"));
        } else {
            tSupp.setText("Tidak ditemukan");
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal mengambil nama supplier");
    }
}


private void getDataTable() {
    model = (DefaultTableModel) table.getModel();
    model.setRowCount(0);
    try {
        Statement stat = connection.createStatement();
        String sql = "SELECT p.id_pembelian, s.nama_barang, sp.nama_supplier, p.jumlah, p.harga_beli, p.tgl_beli, p.status_bayar " +
             "FROM pembelian p " +
             "JOIN suplemen s ON p.kode = s.kode " +
             "JOIN supplier sp ON p.kode_supplier = sp.kode_supp";


        ResultSet res = stat.executeQuery(sql);
        while (res.next()) {
            Object[] obj = new Object[7];
            obj[0] = res.getString("id_pembelian");
            obj[1] = res.getString("nama_barang");
            obj[2] = res.getString("nama_supplier");
            obj[3] = res.getInt("jumlah");
            obj[4] = res.getInt("harga_beli");
            obj[5] = res.getDate("tgl_beli");
            obj[6] = res.getString("status_bayar");
            model.addRow(obj);
        }
    } catch (SQLException err) {
        err.printStackTrace();
    }
}


private void refresh() {
    model = (DefaultTableModel) table.getModel();
    model.setRowCount(0);
    tCari.setText("");
    getDataTable();
}

private void reset() {
    tKode.setText("");
    cbNamaBarang.setSelectedItem("");
    tJumlah.setText("");
    tSupp.setText("");
    tHarga.setText("");
    tKode.setEditable(true);
    bSimpan.setEnabled(true);
}

private void addJumlahListener() {
    tJumlah.addKeyListener(new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
    try {
        String namaBarang = cbNamaBarang.getSelectedItem().toString();
        String jumlahText = tJumlah.getText();

        // Validasi jumlah agar hanya angka dan tidak kosong
        if (!jumlahText.isEmpty() && jumlahText.matches("\\d+")) {
            int jumlah = Integer.parseInt(jumlahText);
            int hargaSatuan = getHargaDariNamaBarang(namaBarang);
            int totalHarga = jumlah * hargaSatuan;

            tHarga.setText(String.valueOf(totalHarga));
        } else {
            tHarga.setText("0"); // Jika kosong atau bukan angka
        }
    } catch (Exception ex) {
        tHarga.setText("0"); // fallback error umum
    }
}

    });
}



private void insert() {
    PreparedStatement statement = null;
    String sql = "INSERT INTO pembelian (id_pembelian, kode, jumlah, harga_beli, tgl_beli, kode_supplier, status_bayar) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try {
        statement = connection.prepareStatement(sql);

        String namaBarang = cbNamaBarang.getSelectedItem().toString();

        statement.setString(1, tKode.getText()); // ✅ id_pembelian dari autoNumber()
        statement.setInt(2, getKodeBarangDariNama(namaBarang)); // kode_barang
        statement.setInt(3, Integer.parseInt(tJumlah.getText())); // jumlah
        int jumlah = Integer.parseInt(tJumlah.getText());
int hargaSatuan = getHargaDariNamaBarang(namaBarang);
int totalHarga = jumlah * hargaSatuan;

statement.setInt(4, totalHarga); // harga_beli (total)

        statement.setDate(5, new java.sql.Date(tTanggal.getDate().getTime())); // tgl_beli
        statement.setString(6, getKodeSuppDariNamaBarang(namaBarang)); // kode_supplier
        statement.setString(7, cbKet.getSelectedItem().toString());

        statement.executeUpdate();
        
        // Kurangi stok suplemen sesuai jumlah pembelian
        String sqlUpdateStok = "UPDATE suplemen SET stok = stok - ? WHERE kode = ?";
        PreparedStatement pstStok = connection.prepareStatement(sqlUpdateStok);
        pstStok.setInt(1, Integer.parseInt(tJumlah.getText())); // jumlah beli
        pstStok.setInt(2, getKodeBarangDariNama(namaBarang));   // kode barang
        pstStok.executeUpdate();

        
        JOptionPane.showMessageDialog(null, "Data berhasil disimpan");

        autoNumber(); // Update ID pembelian berikutnya
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        try {
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}



private int getHargaDariNamaBarang(String namaBarang) {
    int harga = 0;
    try {
        String sql = "SELECT harga FROM suplemen WHERE nama_barang = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, namaBarang);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            harga = rs.getInt("harga");
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return harga;
}

private void loadHargaDanSupplier() {
    try {
        String namaBarang = cbNamaBarang.getSelectedItem().toString();
        Connection con = koneksi.getConnection();
        Statement st = con.createStatement();

        String sql = "SELECT s.kode_supp, sp.nama_supplier, s.harga " +
                     "FROM suplemen s " +
                     "JOIN supplier sp ON s.kode_supp = sp.kode_supp " +
                     "WHERE s.nama_barang = '" + namaBarang + "'";

        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            tSupp.setText(rs.getString("nama_supplier"));
            tHarga.setText(rs.getString("harga")); // Auto tampilkan harga
        } else {
            tSupp.setText("Tidak ditemukan");
            tHarga.setText("");
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal mengambil data supplier dan harga");
    }
}



private int getKodeBarangDariNama(String namaBarang) {
    int kode = -1;
    String sql = "SELECT kode FROM suplemen WHERE nama_barang = ?";
    
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, namaBarang);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            kode = rs.getInt("kode");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return kode;
}


private String getKodeSuppDariNamaBarang(String namaBarang) {
    String kodeSupp = null;
    try {
        String sql = "SELECT sp.kode_supp FROM suplemen s " +
                     "JOIN supplier sp ON s.kode_supp = sp.kode_supp " +
                     "WHERE s.nama_barang = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, namaBarang);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            kodeSupp = rs.getString("kode_supp"); // GUNAKAN getString
        } else {
            JOptionPane.showMessageDialog(null, "Kode supplier tidak ditemukan.");
        }
        rs.close();
        pst.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return kodeSupp;
}




private void cbBarang() {
    try {
        String sql = "SELECT nama_barang FROM suplemen";
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet res = stmt.executeQuery();

        cbNamaBarang.removeAllItems(); // Bersihkan isi sebelumnya

        while (res.next()) {
            cbNamaBarang.addItem(res.getString("nama_barang"));
        }

        res.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


private void autoNumber() {
    try {
        Connection conn = koneksi.getConnection(); 
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id_pembelian) FROM pembelian");

        if (rs.next()) {
            String id = rs.getString(1);
            if (id == null || id.length() < 3) {
                tKode.setText("PB001");
            } else {
                try {
                    int nomor = Integer.parseInt(id.substring(2)) + 1;
                    tKode.setText(String.format("PB%03d", nomor));
                } catch (NumberFormatException e) {
                    // Misal data corrupt seperti "PBxx1"
                    tKode.setText("PB001");
                }
            }
        }
        rs.close();
        st.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}





private void update() {
    PreparedStatement statement = null;
    String sql = "UPDATE pembelian SET kode=?, jumlah=?, harga_beli=?, tgl_beli=?, kode_supplier=?, status_bayar=? WHERE id_pembelian=?";
    
    try {
        statement = connection.prepareStatement(sql);

        String namaBarang = cbNamaBarang.getSelectedItem().toString();
        int kodeBarang = getKodeBarangDariNama(namaBarang);        // ambil kode_barang dari nama
        String kodeSupplier = getKodeSuppDariNamaBarang(namaBarang); // ambil kode_supplier dari nama barang

        statement.setInt(1, kodeBarang);                                        // kode (kode barang)
        statement.setInt(2, Integer.parseInt(tJumlah.getText()));               // jumlah
        statement.setInt(3, Integer.parseInt(tHarga.getText()));                // harga_beli
        statement.setDate(4, new java.sql.Date(tTanggal.getDate().getTime()));  // tgl_beli
        statement.setString(5, kodeSupplier);                                   // kode_supplier
        statement.setString(6, cbKet.getSelectedItem().toString());     // status_bayar dari ComboBox
        statement.setString(7, tKode.getText());                                // id_pembelian (WHERE)

        statement.executeUpdate();
        JOptionPane.showMessageDialog(null, "Data berhasil diperbarui.");
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Update gagal: " + ex.getMessage());
    } finally {
        try {
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}




private void delete() {
    PreparedStatement statement = null;
    String sql = "DELETE FROM pembelian WHERE id_pembelian=?";
    try {
        statement = connection.prepareStatement(sql);
        statement.setString(1, tKode.getText()); // kode_supp
        statement.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        try {
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}


private void search() {
    model = (DefaultTableModel) table.getModel();
    model.setRowCount(0); // Kosongkan tabel sebelum menambahkan hasil pencarian
    PreparedStatement statement = null;

    try {
        String sql = "SELECT p.id_pembelian, s.nama_barang, p.jumlah, p.harga_beli, p.tgl_beli, sp.nama_supplier, p.status_bayar " +
             "FROM pembelian p " +
             "JOIN suplemen s ON p.kode = s.kode " +
             "JOIN supplier sp ON p.kode_supplier = sp.kode_supp " +
             "WHERE s.nama_barang LIKE ? AND p.status_bayar IN ('Menunggu Pembayaran', 'Lunas', 'LUNAS')";


        statement = connection.prepareStatement(sql);
        statement.setString(1, "%" + tCari.getText().trim() + "%");
        ResultSet res = statement.executeQuery();

        while (res.next()) {
            Object[] obj = new Object[7];
            obj[0] = res.getString("id_pembelian");
            obj[1] = res.getString("nama_barang");
            obj[2] = res.getInt("jumlah");
            obj[3] = res.getInt("harga_beli");
            obj[4] = res.getDate("tgl_beli");
            obj[5] = res.getString("nama_supplier");
            obj[6] = res.getString("status_bayar");
            model.addRow(obj);
        }

    } catch (SQLException err) {
        err.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error saat pencarian: " + err.getMessage());
    } finally {
        try {
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
        bSimpan = new javax.swing.JButton();
        bUbah = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        tJumlah = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tCari = new javax.swing.JTextField();
        bRefresh = new javax.swing.JButton();
        bCari = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        bBack = new javax.swing.JButton();
        bHapus = new javax.swing.JButton();
        bReset = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tHarga = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tSupp = new javax.swing.JTextField();
        tTanggal = new com.toedter.calendar.JDateChooser();
        cbNamaBarang = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        cbKet = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Kelola Data Supplier");

        jPanel1.setBackground(new java.awt.Color(251, 249, 249));

        jLabel2.setText("No. Invoice");

        tKode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tKodeActionPerformed(evt);
            }
        });

        jLabel3.setText("Nama Barang");

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

        jLabel9.setText("Jumlah");

        tJumlah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tJumlahActionPerformed(evt);
            }
        });

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

        table.setBackground(new java.awt.Color(204, 204, 255));
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Kode", "Nama Barang", "Supplier", "Jumlah", "Harga", "Tanggal", "Keterangan"
            }
        ));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        bBack.setText("Pembayaran");
        bBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 916, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(tCari, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(bCari)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bRefresh)
                        .addGap(18, 18, 18)
                        .addComponent(bBack)
                        .addGap(19, 19, 19)))
                .addGap(83, 83, 83))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bRefresh)
                    .addComponent(bCari)
                    .addComponent(jLabel5)
                    .addComponent(bBack))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

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

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel1.setText("Add Pemesanan");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1)
        );

        jLabel10.setText("Harga Beli");

        jLabel11.setText("Tanggal");

        jLabel4.setText("Supplier");

        tSupp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tSuppActionPerformed(evt);
            }
        });

        cbNamaBarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbNamaBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbNamaBarangActionPerformed(evt);
            }
        });

        jLabel12.setText("Keterangan");

        cbKet.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Status", "Menunggu Pembayaran", "Lunas" }));
        cbKet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbKetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tSupp, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(69, 69, 69)
                                        .addComponent(jLabel11))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(170, 170, 170)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(bHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(bSimpan))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(bUbah, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(bReset, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(69, 69, 69)
                                        .addComponent(jLabel12)))
                                .addGap(372, 372, 372))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbNamaBarang, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tKode, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(69, 69, 69)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbKet, 0, 288, Short.MAX_VALUE)
                                    .addComponent(tTanggal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tHarga, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
                                .addGap(255, 255, 255))))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 946, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tKode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel10)
                    .addComponent(tHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cbNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(tTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(tSupp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(cbKet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(tJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(101, 101, 101))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bSimpan)
                            .addComponent(bUbah))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bReset)
                            .addComponent(bHapus))
                        .addGap(38, 38, 38)))
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(87, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1010, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tKodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tKodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tKodeActionPerformed

    private void bUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUbahActionPerformed
        // TODO add your handling code here:
       if (!tKode.getText().trim().isEmpty()) { // Pastikan ID pembelian terisi (berarti user udah pilih data)
    if (cbNamaBarang.getSelectedItem() != null &&
        !tJumlah.getText().trim().isEmpty() &&
        !tHarga.getText().trim().isEmpty() &&
        tTanggal.getDate() != null) {

        try {
            update();   // method untuk update data pembelian
            refresh();  // perbarui isi tabel
            reset();    // kosongkan form

            JOptionPane.showMessageDialog(this,
                "Data pembelian berhasil diubah!",
                "Notifikasi", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Terjadi kesalahan saat mengupdate data.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }

    } else {
        JOptionPane.showMessageDialog(this,
            "Lengkapi semua form terlebih dahulu!",
            "Peringatan", JOptionPane.WARNING_MESSAGE);
    }
} else {
    JOptionPane.showMessageDialog(this,
        "Silakan pilih data dari tabel terlebih dahulu!",
        "Peringatan", JOptionPane.WARNING_MESSAGE);
}
    }//GEN-LAST:event_bUbahActionPerformed

    private void bHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHapusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bHapusActionPerformed

    private void bResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bResetActionPerformed
        // TODO add your handling code here:
        reset();
    }//GEN-LAST:event_bResetActionPerformed

    private void tCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tCariActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_tCariActionPerformed

    private void bCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCariActionPerformed
        // TODO add your handling code here:
        search();
    }//GEN-LAST:event_bCariActionPerformed

    private void bRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRefreshActionPerformed
        // TODO add your handling code here:
        refresh();
    }//GEN-LAST:event_bRefreshActionPerformed

    private void bSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSimpanActionPerformed
        // TODO add your handling code here:
       if (!tJumlah.getText().trim().isEmpty() &&
    !tHarga.getText().trim().isEmpty() &&
    cbNamaBarang.getSelectedItem() != null &&
    tTanggal.getDate() != null) {

    insert();   // method untuk menyimpan ke database
    refresh();  // refresh tabel
    reset();    // kosongkan form

    JOptionPane.showMessageDialog(this,
        "Data Pembelian berhasil ditambahkan",
        "Notifikasi", JOptionPane.INFORMATION_MESSAGE);
} else {
    JOptionPane.showMessageDialog(this,
        "Lengkapi form terlebih dahulu!",
        "Notifikasi", JOptionPane.WARNING_MESSAGE);
}


    }//GEN-LAST:event_bSimpanActionPerformed

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        // TODO add your handling code here:
      int selectedRow = table.getSelectedRow();
if (selectedRow != -1) {
    tKode.setText(table.getModel().getValueAt(selectedRow, 0).toString());          // id_pembelian
    cbNamaBarang.setSelectedItem(table.getModel().getValueAt(selectedRow, 1));      // nama_barang
    tJumlah.setText(table.getModel().getValueAt(selectedRow, 3).toString());        // jumlah
    tHarga.setText(table.getModel().getValueAt(selectedRow, 4).toString());         // harga_beli

    // Konversi tanggal dari Object ke java.util.Date lalu set ke JDateChooser
    try {
        Object tanggalObj = table.getModel().getValueAt(selectedRow, 5);
        if (tanggalObj instanceof java.sql.Date) {
            java.sql.Date sqlDate = (java.sql.Date) tanggalObj;
            java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
            tTanggal.setDate(utilDate);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    // Optional: Load supplier berdasarkan barang
    loadHargaDanSupplier();

    // Disable field agar tidak diubah sembarangan
    tKode.setEditable(false);         // ID tidak boleh diubah saat edit
    bSimpan.setEnabled(false);        // Tombol simpan dimatikan saat mode edit
}


    }//GEN-LAST:event_tableMouseClicked

    private void tSuppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tSuppActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tSuppActionPerformed

    private void tJumlahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tJumlahActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tJumlahActionPerformed

    private void cbNamaBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbNamaBarangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbNamaBarangActionPerformed

    private void bBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBackActionPerformed
        // TODO add your handling code here:
         FBayarSPP a = new FBayarSPP();
        a.setVisible(true);
    }//GEN-LAST:event_bBackActionPerformed

    private void cbKetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKetActionPerformed

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
            java.util.logging.Logger.getLogger(FBeli.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FBeli.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FBeli.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FBeli.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FBeli().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBack;
    private javax.swing.JButton bCari;
    private javax.swing.JButton bHapus;
    private javax.swing.JButton bRefresh;
    private javax.swing.JButton bReset;
    private javax.swing.JButton bSimpan;
    private javax.swing.JButton bUbah;
    private javax.swing.JComboBox<String> cbKet;
    private javax.swing.JComboBox<String> cbNamaBarang;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField tCari;
    private javax.swing.JTextField tHarga;
    private javax.swing.JTextField tJumlah;
    private javax.swing.JTextField tKode;
    private javax.swing.JTextField tSupp;
    private com.toedter.calendar.JDateChooser tTanggal;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
