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
public class FBayarSPP extends javax.swing.JFrame {
    Connection connection;
    DefaultTableModel model;
    DefaultTableModel model1, model2;
    String nis, id_pengguna, id_spp;

    /**
     * Creates new form FrameBayar
     */
    public FBayarSPP() {
    initComponents();
    connection = koneksi.getConnection();
    tampilDataPembelian();        
    tampilDataPembayaran();
    Lnis.setText("");         
    tCari.requestFocus();
    refresh();
    }
    
   private void tampilDataPembelian() {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Invoice");
    model.addColumn("Nama Barang");
    model.addColumn("Jumlah");
    model.addColumn("Harga Beli");
    model.addColumn("Status Bayar");

    try {
        String sql = "SELECT pb.id_pembelian, sp.nama_barang, pb.jumlah, pb.harga_beli, pb.status_bayar " +
                     "FROM pembelian pb JOIN suplemen sp ON pb.kode = sp.kode " +
                     "WHERE pb.status_bayar = 'Menunggu Pembayaran'"; // hanya tampilkan yang belum dibayar

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("id_pembelian"),
                rs.getString("nama_barang"),
                rs.getString("jumlah"),
                rs.getString("harga_beli"),
                rs.getString("status_bayar")
            });
        }

        tabel1.setModel(model); // ganti dengan tabel yang kamu pakai
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Gagal menampilkan data pembelian: " + e.getMessage());
    }
}


    
  private void tampilDataPembayaran() {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Tgl Bayar");
    model.addColumn("ID Pembelian");
    model.addColumn("Nama Barang");
    model.addColumn("Bulan");
    model.addColumn("Tahun");
    model.addColumn("Jumlah");

    try {
        String sql = "SELECT p.tgl_bayar, p.id_pembelian, s.nama_barang, p.bulan_dibayar, p.tahun_dibayar, p.jumlah_bayar " +
                     "FROM pembayaran p " +
                     "JOIN suplemen s ON p.kode = s.kode"; // kode = nama_barang
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("tgl_bayar"),
                rs.getString("id_pembelian"),
                rs.getString("nama_barang"),
                rs.getString("bulan_dibayar"),
                rs.getString("tahun_dibayar"),
                rs.getString("jumlah_bayar")
            });
        }

        tabel2.setModel(model);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Gagal menampilkan data pembayaran: " + e.getMessage());
    }
}

    
private void insert() {
    PreparedStatement statement = null;
    String sql = "INSERT INTO pembayaran " +
                 "(id_pembelian, kode, tgl_bayar, bulan_dibayar, tahun_dibayar, jumlah_bayar) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";
    try {
        statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        String id_pembelian = Lnis.getText(); // label id_pembelian
        String kode = getKodeDariNama(Lnama.getText()); // ambil kode dari nama
        String tanggalBayar = java.sql.Date.valueOf(java.time.LocalDate.now()).toString();
        String bulanDibayar = cbBulan.getSelectedItem().toString();
        int tahunDibayar = tTahun.getYear();
        int jumlahBayar = Integer.parseInt(tJumlah.getText());

        // Ambil tagihan dari label
        int totalTagihan = Integer.parseInt(Ltagihan.getText());

        // Validasi apakah uang cukup
        if (jumlahBayar < totalTagihan) {
            JOptionPane.showMessageDialog(null, "Uang yang dibayarkan kurang. Tagihan: Rp " + totalTagihan);
            return; // jangan simpan ke database
        }

        // Lanjut insert jika uang cukup
        statement.setString(1, id_pembelian);
        statement.setString(2, kode);
        statement.setString(3, tanggalBayar);
        statement.setString(4, bulanDibayar);
        statement.setInt(5, tahunDibayar);
        statement.setInt(6, jumlahBayar);

        statement.executeUpdate();
        
        updateStatusBayar(id_pembelian);

        // Hitung kembalian
        int kembalian = jumlahBayar - totalTagihan;
        

        // Tampilkan notifikasi
        if (kembalian > 0) {
            JOptionPane.showMessageDialog(null, "Pembayaran berhasil.\nKembalian Anda: Rp " + kembalian);
        } else {
            JOptionPane.showMessageDialog(null, "Pembayaran berhasil. Tidak ada kembalian.");
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal menyimpan pembayaran: " + ex.getMessage());
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, "Jumlah bayar dan tagihan harus berupa angka.");
    } finally {
        try {
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}


  private String getKodeDariNama(String namaBarang) {
    String kode = "";
    try {
        String sql = "SELECT kode FROM suplemen WHERE nama_barang = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, namaBarang);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            kode = rs.getString("kode");
        }
        rs.close();
        ps.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return kode;
}

    
    // Method merubah nama_pengguna ke id_pengguna
   private String[] getDataPembelian(String kodeBarang) {
    String[] data = new String[2]; // [0] = id_pembelian, [1] = nama_barang
    try {
        PreparedStatement st = connection.prepareStatement(
            "SELECT p.id_pembelian, s.nama_barang " +
            "FROM pembelian p JOIN suplemen s ON p.kode = s.kode " +
            "WHERE s.nama_suplemen = ?"
        );
        st.setString(1, kodeBarang);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            data[0] = rs.getString("id_pembelian");
            data[1] = rs.getString("nama_barang");
        }
    } catch (SQLException ex) {
        Logger.getLogger(FBayarSPP.class.getName()).log(Level.SEVERE, null, ex);
    }
    return data;
}

    
    // Method merubah nominal ke id_spp
   private int getIDPembelian(String harga_beli) {
    int id = 0;
    try {
        PreparedStatement st = connection.prepareStatement(
            "SELECT id_pembelian FROM pembelian WHERE harga_beli = ?"
        );
        st.setString(1, harga_beli);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            id = rs.getInt("id_pembelian");
        }
    } catch (SQLException ex) {
        Logger.getLogger(FBayarSPP.class.getName()).log(Level.SEVERE, null, ex);
    }
    return id;
}

    
     private void ambilDataTableBayar(){
        model2 = (DefaultTableModel) tabel2.getModel();
        model2.setRowCount(0);
        try{
            Statement stat = connection.createStatement();
            String sql = "SELECT * FROM pembayaran, pembelian, suplemen WHERE " +
                         "pembayaran.id_pembelian = pembelian.id_pembelian AND " +
                         "pembayaran.kode = suplemen.kode";

            ResultSet res = stat.executeQuery(sql);
            while(res.next()) {
                Object[] obj = new Object[6];
                obj[0] = res.getString("tgl_bayar");
                obj[1] = res.getString("nama_barang");
                obj[2] = res.getString("bulan_dibayar");
                obj[3] = res.getString("tahun_dibayar");
                obj[4] = res.getString("harga_beli");
                obj[5] = res.getString("jumlah_bayar");
                model2.addRow(obj);
            }
        }catch(SQLException err){
            err.printStackTrace();
        }
    }
     
      private void refresh(){
        model1 = (DefaultTableModel) tabel1.getModel();
        model1.setRowCount(0);
        tCari.setText("");
        tampilDataPembelian();
        tampilDataPembayaran();
        
    }
      
  private void search() {
    model1 = (DefaultTableModel) tabel1.getModel();
    model1.setRowCount(0); // Bersihkan data lama
    PreparedStatement statement = null;

    try {
        String sql = "SELECT p.id_pembelian, s.nama_suplemen, p.jumlah, p.harga_beli " +
                     "FROM pembelian p JOIN suplemen s ON p.kode = s.kode " +
                     "WHERE p.id_pembelian LIKE ?";
        statement = connection.prepareStatement(sql);
        statement.setString(1, "%" + tCari.getText() + "%");
        ResultSet res = statement.executeQuery();

        while (res.next()) {
            Object[] obj = new Object[4];
            obj[0] = res.getString("id_pembelian");
            obj[1] = res.getString("nama_suplemen");
            obj[2] = res.getString("jumlah");
            obj[3] = res.getString("harga_beli");
            model1.addRow(obj);
        }
    } catch (SQLException err) {
        err.printStackTrace();
    }
}
  
  private void updateStatusBayar(String idPembelian) {
    try {
        String sql = "UPDATE pembelian SET status_bayar = 'Lunas' WHERE id_pembelian = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, idPembelian);
        ps.executeUpdate();
        
        ps.close();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Gagal update status bayar: " + e.getMessage());
    }
}


      
     private void reset() {
    Lnis.setText("");        // ID Pembelian
    Lnama.setText("");       // Nama Barang
    Ltagihan.setText("");    // Harga
    tJumlah.setText("");     // Jumlah bayar
    bBayar.setEnabled(true); // Aktifkan tombol bayar
}



    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        tCari = new javax.swing.JTextField();
        bCari = new javax.swing.JButton();
        bESiswa = new javax.swing.JButton();
        bRefresh = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabel1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabel2 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbBulan = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        bBayar = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tJumlah = new javax.swing.JTextField();
        Ltagihan = new javax.swing.JLabel();
        Lnis = new javax.swing.JLabel();
        Lnama = new javax.swing.JLabel();
        tTanggal = new com.toedter.calendar.JDateChooser();
        tTahun = new com.toedter.calendar.JYearChooser();

        jLabel7.setText("Bulan");

        jLabel11.setText("Jumlah Dibayar");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(251, 249, 249));

        jLabel1.setText("No. Invoice");

        jLabel2.setText("Pencarian Data");

        jLabel3.setText("Transaksi Pembayaran");

        tCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tCariActionPerformed(evt);
            }
        });

        bCari.setText("Cari");
        bCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCariActionPerformed(evt);
            }
        });

        bESiswa.setText("Entry Beli");
        bESiswa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bESiswaActionPerformed(evt);
            }
        });

        bRefresh.setText("Refresh");
        bRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRefreshActionPerformed(evt);
            }
        });

        tabel1.setBackground(new java.awt.Color(204, 204, 255));
        tabel1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "No.Invoice", "Nama Barang", "Jumlah", "Nominal", "Keterangan"
            }
        ));
        tabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabel1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabel1);

        tabel2.setBackground(new java.awt.Color(204, 204, 255));
        tabel2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Tgl Bayar", "No Invoice", "Bulan", "Tahun", "Tagihan", "Dibayar"
            }
        ));
        jScrollPane2.setViewportView(tabel2);

        jLabel4.setText("Tanggal Bayar");

        jLabel5.setText("Invoice");

        cbBulan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "Februari", "Maret", "April ", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November ", "Desember" }));
        cbBulan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbBulanActionPerformed(evt);
            }
        });

        jLabel6.setText("Bulan");

        bBayar.setText("Bayar");
        bBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBayarActionPerformed(evt);
            }
        });

        jLabel8.setText("Tahun");

        jLabel9.setText("Tagihan");

        jLabel10.setText("Jumlah Dibayar");

        tJumlah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tJumlahActionPerformed(evt);
            }
        });

        Ltagihan.setText("Rp.0.00");

        Lnis.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        Lnis.setText("###");

        Lnama.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        Lnama.setText("#####");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(cbBulan, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(tTahun, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tJumlah)
                            .addComponent(bBayar, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                            .addComponent(Ltagihan)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(Lnis)
                                .addGap(18, 18, 18)
                                .addComponent(Lnama))
                            .addComponent(tTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(tTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(Lnis)
                    .addComponent(Lnama))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbBulan, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tTahun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(Ltagihan))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(tJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bBayar)
                .addGap(103, 103, 103))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tCari, javax.swing.GroupLayout.PREFERRED_SIZE, 465, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(bCari)
                        .addGap(18, 18, 18)
                        .addComponent(bRefresh)
                        .addGap(18, 18, 18)
                        .addComponent(bESiswa)
                        .addContainerGap(240, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane2))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addContainerGap())))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(35, 35, 35)
                    .addComponent(jLabel3)
                    .addContainerGap(934, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bCari)
                    .addComponent(bESiswa)
                    .addComponent(bRefresh))
                .addGap(41, 41, 41)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(48, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(34, 34, 34)
                    .addComponent(jLabel3)
                    .addContainerGap(772, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tCariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tCariActionPerformed

    private void bCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCariActionPerformed
        // TODO add your handling code here:
        tampilDataPembelian();
        tampilDataPembayaran();
        if(!tCari.getText().trim().isEmpty()) {
            model1 = (DefaultTableModel) tabel1.getModel();
            model1.setRowCount(0);
            search();
        } else {
            JOptionPane.showMessageDialog(this,
                "Masukan nis yang ingin dicari!",
                "Notifikasi", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_bCariActionPerformed

    private void bESiswaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bESiswaActionPerformed
        // TODO add your handling code here:
        FBeli a = new FBeli();
        a.setVisible(true);
    }//GEN-LAST:event_bESiswaActionPerformed

    private void bRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRefreshActionPerformed
        // TODO add your handling code here:
        refresh();
        tampilDataPembelian(); // refresh tabel pembelian
tampilDataPembayaran(); // refresh tabel pembayaran

    }//GEN-LAST:event_bRefreshActionPerformed

    private void cbBulanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbBulanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbBulanActionPerformed

    private void bBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBayarActionPerformed
        // TODO add your handling code here:
        insert();
        reset();
        tampilDataPembayaran();
        updateStatusBayar(Lnis.getText());
    }//GEN-LAST:event_bBayarActionPerformed

    private void tJumlahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tJumlahActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tJumlahActionPerformed

    private void tabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabel1MouseClicked
        // TODO add your handling code here:
int selectedRow = tabel1.getSelectedRow();
if (selectedRow >= 0) {
    // Ambil id_pembelian dari kolom pertama
    String idPembelian = tabel1.getValueAt(selectedRow, 0).toString().trim();
    System.out.println("ID Pembelian: '" + idPembelian + "'");

    try {
        String sql = "SELECT p.id_pembelian, s.nama_barang, p.harga_beli " +
                     "FROM pembelian p " +
                     "JOIN suplemen s ON p.kode = s.kode " +
                     "WHERE p.id_pembelian = ?";  // ubah ke p.id_pembelian

        PreparedStatement st = connection.prepareStatement(sql);
        st.setString(1, idPembelian);  // id_pembelian adalah varchar

        ResultSet rs = st.executeQuery();

        if (rs.next()) {
            Lnis.setText(rs.getString("id_pembelian"));    // dari pembelian
            Lnama.setText(rs.getString("nama_barang"));    // dari suplemen
            Ltagihan.setText(rs.getString("harga_beli"));  // dari pembelian
        } else {
            JOptionPane.showMessageDialog(null, "Data tidak ditemukan untuk kode: " + idPembelian);
        }

        rs.close();
        st.close();

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal mengambil data suplemen: " + e.getMessage());
    }
} else {
    JOptionPane.showMessageDialog(null, "Silakan pilih baris data terlebih dahulu.");
}



    }//GEN-LAST:event_tabel1MouseClicked

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
            java.util.logging.Logger.getLogger(FBayarSPP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FBayarSPP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FBayarSPP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FBayarSPP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FBayarSPP().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Lnama;
    private javax.swing.JLabel Lnis;
    private javax.swing.JLabel Ltagihan;
    private javax.swing.JButton bBayar;
    private javax.swing.JButton bCari;
    private javax.swing.JButton bESiswa;
    private javax.swing.JButton bRefresh;
    private javax.swing.JComboBox<String> cbBulan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField tCari;
    private javax.swing.JTextField tJumlah;
    private com.toedter.calendar.JYearChooser tTahun;
    private com.toedter.calendar.JDateChooser tTanggal;
    private javax.swing.JTable tabel1;
    private javax.swing.JTable tabel2;
    // End of variables declaration//GEN-END:variables

}
