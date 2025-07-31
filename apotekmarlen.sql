-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 14, 2025 at 05:44 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `apotekmarlen`
--

-- --------------------------------------------------------

--
-- Table structure for table `jenis`
--

CREATE TABLE `jenis` (
  `kode_jenis` int(11) NOT NULL,
  `jenis` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `jenis`
--

INSERT INTO `jenis` (`kode_jenis`, `jenis`) VALUES
(1, 'Suplemen Otak'),
(2, 'Suplemen Darah'),
(3, 'Suplemen Mineral');

-- --------------------------------------------------------

--
-- Table structure for table `pembayaran`
--

CREATE TABLE `pembayaran` (
  `id_pembayaran` int(11) NOT NULL,
  `id_pembelian` varchar(10) NOT NULL,
  `kode` int(11) NOT NULL,
  `tgl_bayar` date NOT NULL,
  `bulan_dibayar` varchar(8) NOT NULL,
  `tahun_dibayar` varchar(4) NOT NULL,
  `jumlah_bayar` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pembayaran`
--

INSERT INTO `pembayaran` (`id_pembayaran`, `id_pembelian`, `kode`, `tgl_bayar`, `bulan_dibayar`, `tahun_dibayar`, `jumlah_bayar`) VALUES
(1, 'PB001', 1, '2025-06-14', 'January', '2025', 2000000),
(2, 'PB001', 1, '2025-06-14', 'January', '2025', 2000000),
(3, 'PB002', 5, '2025-06-14', 'January', '2025', 500000),
(4, 'PB003', 2, '2025-06-14', 'January', '2025', 215000),
(5, 'PB004', 4, '2025-06-14', 'January', '2025', 415000),
(6, 'PB004', 4, '2025-06-14', 'January', '2025', 415000),
(7, 'PB005', 6, '2025-06-14', 'January', '2025', 120000),
(8, 'PB006', 7, '2025-06-14', 'Juni', '2025', 70000);

-- --------------------------------------------------------

--
-- Table structure for table `pembelian`
--

CREATE TABLE `pembelian` (
  `id_pembelian` varchar(10) NOT NULL,
  `kode` int(11) NOT NULL,
  `jumlah` int(11) NOT NULL,
  `harga_beli` int(11) NOT NULL,
  `tgl_beli` date NOT NULL,
  `kode_supplier` char(8) NOT NULL,
  `status_bayar` varchar(20) NOT NULL DEFAULT 'Menunggu Pembayaran'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pembelian`
--

INSERT INTO `pembelian` (`id_pembelian`, `kode`, `jumlah`, `harga_beli`, `tgl_beli`, `kode_supplier`, `status_bayar`) VALUES
('4', 6, 1, 120000, '2025-06-13', '2', 'Lunas'),
('PB001', 1, 2, 1700000, '2025-06-13', '1', 'LUNAS'),
('PB002', 5, 1, 350000, '2025-06-14', '5', 'LUNAS'),
('PB003', 2, 1, 215000, '2025-06-14', '1', 'Lunas'),
('PB004', 4, 1, 415000, '2025-06-14', '1', 'Lunas'),
('PB005', 6, 1, 120000, '2025-06-14', '2', 'Lunas'),
('PB006', 7, 1, 70000, '2025-06-14', '6', 'Lunas');

-- --------------------------------------------------------

--
-- Table structure for table `pengguna`
--

CREATE TABLE `pengguna` (
  `id_pengguna` int(11) NOT NULL,
  `username` varchar(25) NOT NULL,
  `password` varchar(32) NOT NULL,
  `nama_pengguna` varchar(35) NOT NULL,
  `level` enum('admin','petugas','siswa') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pengguna`
--

INSERT INTO `pengguna` (`id_pengguna`, `username`, `password`, `nama_pengguna`, `level`) VALUES
(1, 'helen', 'helen', 'helen', 'admin'),
(2, 'maria', 'maria', 'Petugas', 'petugas'),
(3, 'siswa', 'siswa', 'Siswa', 'siswa');

-- --------------------------------------------------------

--
-- Table structure for table `suplemen`
--

CREATE TABLE `suplemen` (
  `kode` int(11) NOT NULL,
  `nama_barang` varchar(50) NOT NULL,
  `keterangan` varchar(50) NOT NULL,
  `harga` int(11) NOT NULL,
  `stok` int(11) NOT NULL,
  `kode_jenis` int(11) DEFAULT NULL,
  `kode_supp` char(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `suplemen`
--

INSERT INTO `suplemen` (`kode`, `nama_barang`, `keterangan`, `harga`, `stok`, `kode_jenis`, `kode_supp`) VALUES
(1, 'Focus Factor', 'Exp: 6/2028', 850000, 49, 1, '1'),
(2, 'Natures Plus Folic A', 'Exp: 5/2027', 215000, 40, 3, '1'),
(4, 'Natures Way Kids Cool', 'EXP: 9/2029', 415000, 70, 3, '1'),
(5, 'Nordic Baby DHA', 'EXP: 8/2026', 350000, 10, 2, '5'),
(6, 'Imboost', 'EXP: 6/2029', 120000, 10, 1, '2'),
(7, 'Renovit', 'EXP: 9/2027', 70000, 10, 3, '6');

-- --------------------------------------------------------

--
-- Table structure for table `supplier`
--

CREATE TABLE `supplier` (
  `kode_supp` char(8) NOT NULL,
  `nama_supplier` varchar(35) NOT NULL,
  `alamat` text NOT NULL,
  `no_telp` varchar(13) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `supplier`
--

INSERT INTO `supplier` (`kode_supp`, `nama_supplier`, `alamat`, `no_telp`) VALUES
('1', 'i Herb', 'Karawaci', '0818288890'),
('2', 'Radian', 'Cimone', '081234321212'),
('3', 'Natures Way', 'Tangerang', '0897612134457'),
('4', 'Nordic Plus', 'Jakarta', '089613392326'),
('5', 'Labore', 'Jakarta', '081214133555'),
('6', 'Konimex', 'Jakarta', '081288852367');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `jenis`
--
ALTER TABLE `jenis`
  ADD PRIMARY KEY (`kode_jenis`);

--
-- Indexes for table `pembayaran`
--
ALTER TABLE `pembayaran`
  ADD PRIMARY KEY (`id_pembayaran`),
  ADD KEY `id_pembelian` (`id_pembelian`),
  ADD KEY `kode` (`kode`);

--
-- Indexes for table `pembelian`
--
ALTER TABLE `pembelian`
  ADD PRIMARY KEY (`id_pembelian`),
  ADD KEY `pembelian_ibfk_2` (`kode_supplier`),
  ADD KEY `pembelian_ibfk_1` (`kode`);

--
-- Indexes for table `pengguna`
--
ALTER TABLE `pengguna`
  ADD PRIMARY KEY (`id_pengguna`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `suplemen`
--
ALTER TABLE `suplemen`
  ADD PRIMARY KEY (`kode`),
  ADD KEY `fk_jenis` (`kode_jenis`),
  ADD KEY `fk_supplier` (`kode_supp`);

--
-- Indexes for table `supplier`
--
ALTER TABLE `supplier`
  ADD PRIMARY KEY (`kode_supp`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `jenis`
--
ALTER TABLE `jenis`
  MODIFY `kode_jenis` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `pembayaran`
--
ALTER TABLE `pembayaran`
  MODIFY `id_pembayaran` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `pengguna`
--
ALTER TABLE `pengguna`
  MODIFY `id_pengguna` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `suplemen`
--
ALTER TABLE `suplemen`
  MODIFY `kode` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `pembayaran`
--
ALTER TABLE `pembayaran`
  ADD CONSTRAINT `pembayaran_ibfk_1` FOREIGN KEY (`id_pembelian`) REFERENCES `pembelian` (`id_pembelian`),
  ADD CONSTRAINT `pembayaran_ibfk_2` FOREIGN KEY (`kode`) REFERENCES `suplemen` (`kode`);

--
-- Constraints for table `pembelian`
--
ALTER TABLE `pembelian`
  ADD CONSTRAINT `pembelian_ibfk_1` FOREIGN KEY (`kode`) REFERENCES `suplemen` (`kode`),
  ADD CONSTRAINT `pembelian_ibfk_2` FOREIGN KEY (`kode_supplier`) REFERENCES `supplier` (`kode_supp`);

--
-- Constraints for table `suplemen`
--
ALTER TABLE `suplemen`
  ADD CONSTRAINT `fk_jenis` FOREIGN KEY (`kode_jenis`) REFERENCES `jenis` (`kode_jenis`),
  ADD CONSTRAINT `fk_supplier` FOREIGN KEY (`kode_supp`) REFERENCES `supplier` (`kode_supp`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
